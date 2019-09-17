package io.cordacity.koncerto.workflow.relationship

import co.paralleluniverse.fibers.Suspendable
import io.cordacity.koncerto.contract.relationship.RelationshipContract
import io.cordacity.koncerto.contract.relationship.RelationshipState
import io.cordacity.koncerto.workflow.*
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step

class RevokeRelationshipFlow(
    private val relationship: StateAndRef<RelationshipState<*>>,
    private val notary: Party,
    private val sessions: Collection<FlowSession> = emptyList(),
    override val progressTracker: ProgressTracker = tracker()
) : FlowLogic<SignedTransaction>() {

    companion object {
        private const val FLOW_VERSION_1 = 1

        @JvmStatic
        fun tracker() = ProgressTracker(INITIALIZING, GENERATING, VERIFYING, SIGNING, GATHERING, FINALIZING)
    }

    @Suspendable
    override fun call(): SignedTransaction {
        currentStep(INITIALIZING)
        checkSessionsForAllCounterparties(relationship.state.data, sessions)
        sessions.forEach { it.send(Pair(FlowAction.REVOKING, false)) }

        currentStep(GENERATING)
        val transaction = with(TransactionBuilder(notary)) {
            addInputState(relationship)
            addCommand(RelationshipContract.Revoke, keysFor(relationship.state.data.participants))
        }

        currentStep(VERIFYING)
        transaction.verify(serviceHub)

        currentStep(SIGNING)
        val partiallySignedTransaction = serviceHub.signInitialTransaction(transaction, ourIdentity.owningKey)

        currentStep(GATHERING)
        val fullySignedTransaction = subFlow(
            CollectSignaturesFlow(
                partiallySignedTransaction,
                sessions,
                GATHERING.childProgressTracker()
            )
        )

        currentStep(FINALIZING)
        return subFlow(FinalityFlow(fullySignedTransaction, sessions, FINALIZING.childProgressTracker()))
    }

    @StartableByRPC
    @StartableByService
    @InitiatingFlow(version = FLOW_VERSION_1)
    class Initiator(
        private val relationship: StateAndRef<RelationshipState<*>>,
        private val notary: Party? = null
    ) : FlowLogic<SignedTransaction>() {

        private companion object {
            object REVOKING : Step("Revoking relationship.") {
                override fun childProgressTracker() = tracker()
            }
        }

        override val progressTracker = ProgressTracker(REVOKING)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(REVOKING)
            val sessions = flowSessionsFor(relationship.state.data.participants - ourIdentity)
            return subFlow(
                RevokeRelationshipFlow(
                    relationship,
                    notary ?: firstNotary,
                    sessions,
                    REVOKING.childProgressTracker()
                )
            )
        }
    }

    @InitiatedBy(Initiator::class)
    class Observer(private val session: FlowSession) : FlowLogic<SignedTransaction>() {

        private companion object {
            object OBSERVING : Step("Observing relationship revocation.") {
                override fun childProgressTracker() = ObserveRelationshipFlow.tracker()
            }
        }

        override val progressTracker = ProgressTracker(OBSERVING)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(OBSERVING)
            return subFlow(ObserveRelationshipFlow(session, OBSERVING.childProgressTracker()))
        }
    }
}