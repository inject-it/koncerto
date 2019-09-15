package io.cordacity.koncerto.workflow.relationship

import co.paralleluniverse.fibers.Suspendable
import io.cordacity.koncerto.contract.relationship.RelationshipContract
import io.cordacity.koncerto.contract.relationship.RelationshipState
import io.cordacity.koncerto.contract.revocation.RevocationLockContract
import io.cordacity.koncerto.contract.revocation.RevocationLockState
import io.cordacity.koncerto.workflow.*
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step

class IssueRelationshipFlow(
    private val relationship: RelationshipState<*>,
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
        checkSessionsForAllCounterparties(relationship, sessions)
        sessions.forEach { it.send(Pair(FlowAction.ISSUING, checkMembership)) }

        if (checkMembership) {
            checkMembershipsAndAttestations(relationship.participants - ourIdentity, relationship.network)
        }

        currentStep(GENERATING)
        val transaction = with(TransactionBuilder(notary)) {
            relationship.participants.forEach {
                addOutputState(RevocationLockState.create(it, relationship), RevocationLockContract.ID)
            }
            addCommand(RevocationLockContract.Create, keysFor(relationship.participants))
            addOutputState(relationship, RelationshipContract.ID)
            addCommand(RelationshipContract.Issue, keysFor(relationship.participants))
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
        private val relationship: RelationshipState<*>,
        private val notary: Party? = null,
        private val checkMembership: Boolean = false
    ) : FlowLogic<SignedTransaction>() {

        private companion object {
            object ISSUING : Step("Issuing relationship.") {
                override fun childProgressTracker() = tracker()
            }
        }

        override val progressTracker = ProgressTracker(ISSUING)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(ISSUING)
            val sessions = flowSessionsFor(relationship.participants - ourIdentity)
            return subFlow(
                IssueRelationshipFlow(
                    relationship,
                    notary ?: firstNotary,
                    sessions,
                    checkMembership,
                    ISSUING.childProgressTracker()
                )
            )
        }
    }

    @InitiatedBy(Initiator::class)
    class Observer(private val session: FlowSession) : FlowLogic<SignedTransaction>() {

        private companion object {
            object OBSERVING : Step("Observing relationship issuance.") {
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