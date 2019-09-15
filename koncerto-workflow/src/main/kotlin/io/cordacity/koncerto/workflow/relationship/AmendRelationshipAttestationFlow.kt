package io.cordacity.koncerto.workflow.relationship

import co.paralleluniverse.fibers.Suspendable
import io.cordacity.koncerto.contract.relationship.RelationshipAttestationContract
import io.cordacity.koncerto.contract.relationship.RelationshipAttestationState
import io.cordacity.koncerto.workflow.*
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

class AmendRelationshipAttestationFlow(
    private val oldAttestation: StateAndRef<RelationshipAttestationState>,
    private val newAttestation: RelationshipAttestationState,
    private val notary: Party,
    private val sessions: Collection<FlowSession>,
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
        checkSessionsForAllCounterparties(newAttestation, sessions)

        currentStep(GENERATING)
        val transaction = with(TransactionBuilder(notary)) {
            addInputState(oldAttestation)
            addOutputState(newAttestation, RelationshipAttestationContract.ID)
            addReferenceState(newAttestation.pointer.resolve(serviceHub).referenced())
            addCommand(RelationshipAttestationContract.Amend, keysFor(newAttestation.participants))
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
        private val oldAttestation: StateAndRef<RelationshipAttestationState>,
        private val newAttestation: RelationshipAttestationState,
        private val notary: Party? = null
    ) : FlowLogic<SignedTransaction>() {

        private companion object {
            object AMENDING : ProgressTracker.Step("Amending relationship attestation.") {
                override fun childProgressTracker() = tracker()
            }
        }

        override val progressTracker = ProgressTracker(AMENDING)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(AMENDING)
            val session = flowSessionsFor(newAttestation.attestees)
            return subFlow(
                AmendRelationshipAttestationFlow(
                    oldAttestation,
                    newAttestation,
                    notary ?: firstNotary,
                    session,
                    AMENDING.childProgressTracker()
                )
            )
        }
    }

    @InitiatedBy(Initiator::class)
    class Observer(private val session: FlowSession) : FlowLogic<SignedTransaction>() {

        private companion object {
            object OBSERVING : ProgressTracker.Step("Observing relationship attestation amendment.") {
                override fun childProgressTracker() = ObserveRelationshipAttestationFlow.tracker()
            }
        }

        override val progressTracker = ProgressTracker(OBSERVING)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(OBSERVING)
            return subFlow(ObserveRelationshipAttestationFlow(session, OBSERVING.childProgressTracker()))
        }
    }
}