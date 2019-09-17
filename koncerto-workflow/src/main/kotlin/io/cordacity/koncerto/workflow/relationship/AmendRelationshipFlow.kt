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

class AmendRelationshipFlow(
    private val oldRelationship: StateAndRef<RelationshipState<*>>,
    private val newRelationship: RelationshipState<*>,
    private val notary: Party,
    private val sessions: Collection<FlowSession> = emptyList(),
    private val checkMembership: Boolean = false,
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
        checkSessionsForAllCounterparties(newRelationship, sessions)
        sessions.forEach { it.send(Pair(FlowAction.AMENDING, checkMembership)) }

        if (checkMembership) {
            checkMembershipsAndAttestations(newRelationship.participants - ourIdentity, newRelationship.network)
        }

        val signingKeys = keysFor(oldRelationship.state.data.participants union newRelationship.participants)

        currentStep(GENERATING)
        val transaction = with(TransactionBuilder(notary)) {
            addInputState(oldRelationship)
            addOutputState(newRelationship, RelationshipContract.ID)
            addCommand(RelationshipContract.Amend, signingKeys)
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
        private val oldRelationship: StateAndRef<RelationshipState<*>>,
        private val newRelationship: RelationshipState<*>,
        private val notary: Party? = null,
        private val checkMembership: Boolean = false
    ) : FlowLogic<SignedTransaction>() {

        private companion object {
            object AMENDING : Step("Amending relationship.") {
                override fun childProgressTracker() = tracker()
            }
        }

        override val progressTracker = ProgressTracker(AMENDING)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(AMENDING)
            val sessions = flowSessionsFor(newRelationship.participants - ourIdentity)
            return subFlow(
                AmendRelationshipFlow(
                    oldRelationship,
                    newRelationship,
                    notary ?: firstNotary,
                    sessions,
                    checkMembership,
                    AMENDING.childProgressTracker()
                )
            )
        }
    }

    @InitiatedBy(Initiator::class)
    class Observer(private val session: FlowSession) : FlowLogic<SignedTransaction>() {

        private companion object {
            object OBSERVING : Step("Observing relationship amendment.") {
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