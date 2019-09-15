package io.cordacity.koncerto.workflow.membership

import co.paralleluniverse.fibers.Suspendable
import io.cordacity.koncerto.contract.membership.MembershipContract
import io.cordacity.koncerto.contract.membership.MembershipState
import io.cordacity.koncerto.workflow.*
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step

class IssueMembershipFlow(
    private val membership: MembershipState<*>,
    private val notary: Party,
    private val sessions: Collection<FlowSession> = emptyList(),
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
        checkSessionsForAllCounterparties(membership, sessions)

        currentStep(GENERATING)
        val transaction = with(TransactionBuilder(notary)) {
            addOutputState(membership, MembershipContract.ID)
            addCommand(MembershipContract.Issue, ourIdentity.owningKey)
        }

        currentStep(VERIFYING)
        transaction.verify(serviceHub)

        currentStep(SIGNING)
        val signedTransaction = serviceHub.signInitialTransaction(transaction, ourIdentity.owningKey)

        currentStep(FINALIZING)
        return subFlow(FinalityFlow(signedTransaction, sessions, FINALIZING.childProgressTracker()))
    }

    @StartableByRPC
    @StartableByService
    @InitiatingFlow(version = FLOW_VERSION_1)
    class Initiator(
        private val membership: MembershipState<*>,
        private val notary: Party? = null,
        private val observers: Set<Party> = emptySet()
    ) : FlowLogic<SignedTransaction>() {

        private companion object {
            object ISSUING : Step("Issuing membership.") {
                override fun childProgressTracker() = tracker()
            }
        }

        override val progressTracker = ProgressTracker(ISSUING)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(ISSUING)
            val sessions = flowSessionsFor(observers)
            return subFlow(
                IssueMembershipFlow(
                    membership,
                    notary ?: firstNotary,
                    sessions,
                    ISSUING.childProgressTracker()
                )
            )
        }
    }

    @InitiatedBy(Initiator::class)
    class Observer(private val session: FlowSession) : FlowLogic<SignedTransaction>() {

        private companion object {
            object OBSERVING : Step("Observing membership issuance.") {
                override fun childProgressTracker() = ObserveMembershipFlow.tracker()
            }
        }

        override val progressTracker = ProgressTracker(OBSERVING)

        override fun call(): SignedTransaction {
            currentStep(OBSERVING)
            return subFlow(ObserveMembershipFlow(session, null, OBSERVING.childProgressTracker()))
        }
    }
}