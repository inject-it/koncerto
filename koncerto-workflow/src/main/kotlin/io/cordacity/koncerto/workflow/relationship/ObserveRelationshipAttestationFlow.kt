package io.cordacity.koncerto.workflow.relationship

import co.paralleluniverse.fibers.Suspendable
import io.cordacity.koncerto.contract.relationship.RelationshipAttestationState
import io.cordacity.koncerto.contract.relationship.RelationshipState
import io.cordacity.koncerto.workflow.COUNTERSIGNING
import io.cordacity.koncerto.workflow.FINALIZING
import io.cordacity.koncerto.workflow.currentStep
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker

class ObserveRelationshipAttestationFlow(
    private val session: FlowSession,
    override val progressTracker: ProgressTracker = tracker()
) : FlowLogic<SignedTransaction>() {

    companion object {
        @JvmStatic
        fun tracker() = ProgressTracker(COUNTERSIGNING, FINALIZING)
    }

    @Suspendable
    override fun call(): SignedTransaction {
        currentStep(COUNTERSIGNING)
        val transaction = subFlow(object : SignTransactionFlow(session) {
            override fun checkTransaction(stx: SignedTransaction) {
//                if (stx.tx.outputsOfType<RelationshipAttestationState>().isNotEmpty()) {
//                    val relationshipState = serviceHub
//                        .toStateAndRef<RelationshipState<*>>(stx.tx.references.single()).state.data
//
//                    if (ourIdentity !in relationshipState.participants) {
//                        throw FlowException("Relationship is not relevant to counter-party: $ourIdentity.")
//                    }
//                }
            }
        })

        currentStep(FINALIZING)
        return subFlow(ReceiveFinalityFlow(session, transaction.id))
    }
}