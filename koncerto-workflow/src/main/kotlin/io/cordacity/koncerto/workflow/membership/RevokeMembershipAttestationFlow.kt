package io.cordacity.koncerto.workflow.membership

import co.paralleluniverse.fibers.Suspendable
import io.cordacity.koncerto.contract.membership.MembershipAttestationContract
import io.cordacity.koncerto.contract.membership.MembershipAttestationState
import io.cordacity.koncerto.workflow.*
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step

class RevokeMembershipAttestationFlow(
    private val attestation: StateAndRef<MembershipAttestationState>,
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
        if (session.counterparty != attestation.state.data.attestee) {
            throw FlowException("A flow session is required for the attestee: ${attestation.state.data.attestee}.")
        }

        currentStep(GENERATING)
        val transaction = with(TransactionBuilder(notary)) {
            addInputState(attestation)
            addCommand(MembershipAttestationContract.Revoke, ourIdentity.owningKey)
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
        private val attestation: StateAndRef<MembershipAttestationState>,
        private val notary: Party? = null
    ) : FlowLogic<SignedTransaction>() {

        private companion object {
            object REVOKING : Step("Revoking membership attestation.") {
                override fun childProgressTracker() = tracker()
            }
        }

        override val progressTracker = ProgressTracker(REVOKING)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(REVOKING)
            val session = flowSessionFor(attestation.state.data.attestee)
            return subFlow(
                RevokeMembershipAttestationFlow(
                    attestation,
                    notary ?: firstNotary,
                    session,
                    REVOKING.childProgressTracker()
                )
            )
        }
    }

    @InitiatedBy(Initiator::class)
    class Observer(private val session: FlowSession) : FlowLogic<SignedTransaction>() {

        private companion object {
            object OBSERVING : Step("Observing membership attestation revocation.") {
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