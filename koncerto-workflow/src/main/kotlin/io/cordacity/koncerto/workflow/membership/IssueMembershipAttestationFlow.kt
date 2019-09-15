package io.cordacity.koncerto.workflow.membership

import co.paralleluniverse.fibers.Suspendable
import io.cordacity.koncerto.contract.membership.MembershipAttestationContract
import io.cordacity.koncerto.contract.membership.MembershipAttestationState
import io.cordacity.koncerto.workflow.*
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step

class IssueMembershipAttestationFlow(
    private val attestation: MembershipAttestationState,
    private val notary: Party,
    private val session: FlowSession,
    override val progressTracker: ProgressTracker = tracker()
) : FlowLogic<SignedTransaction>() {

    companion object {
        private const val FLOW_VERSION_1 = 1

        @JvmStatic
        fun tracker() = ProgressTracker(INITIALIZING, GENERATING, VERIFYING, SIGNING, FINALIZING)
    }

    @Suspendable
    override fun call(): SignedTransaction {
        currentStep(INITIALIZING)
        if (session.counterparty != attestation.attestee) {
            throw FlowException("A flow session is required for the attestee: ${attestation.attestee}.")
        }

        currentStep(GENERATING)
        val transaction = with(TransactionBuilder(notary)) {
            addOutputState(attestation, MembershipAttestationContract.ID)
            addReferenceState(attestation.pointer.resolve(serviceHub).referenced())
            addCommand(MembershipAttestationContract.Issue, ourIdentity.owningKey)
        }

        currentStep(VERIFYING)
        transaction.verify(serviceHub)

        currentStep(SIGNING)
        val signedTransaction = serviceHub.signInitialTransaction(transaction, ourIdentity.owningKey)

        currentStep(FINALIZING)
        return subFlow(FinalityFlow(signedTransaction, listOf(session), FINALIZING.childProgressTracker()))
    }

    @StartableByRPC
    @StartableByService
    @InitiatingFlow(version = FLOW_VERSION_1)
    class Initiator(
        private val attestation: MembershipAttestationState,
        private val notary: Party? = null
    ) : FlowLogic<SignedTransaction>() {

        private companion object {
            object ISSUING : Step("Issuing membership attestation.") {
                override fun childProgressTracker() = tracker()
            }
        }

        override val progressTracker = ProgressTracker(ISSUING)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(ISSUING)
            val session = flowSessionFor(attestation.attestee)
            return subFlow(
                IssueMembershipAttestationFlow(
                    attestation,
                    notary ?: firstNotary,
                    session,
                    ISSUING.childProgressTracker()
                )
            )
        }
    }

    @InitiatedBy(Initiator::class)
    class Observer(private val session: FlowSession) : FlowLogic<SignedTransaction>() {

        private companion object {
            object OBSERVING : Step("Observing membership attestation.") {
                override fun childProgressTracker() = ObserveMembershipAttestationFlow.tracker()
            }
        }

        override val progressTracker = ProgressTracker(OBSERVING)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(OBSERVING)
            return subFlow(ObserveMembershipAttestationFlow(session, null, OBSERVING.childProgressTracker()))
        }
    }
}