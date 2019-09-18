package io.cordacity.koncerto.workflow.relationship

import co.paralleluniverse.fibers.Suspendable
import io.cordacity.koncerto.contract.relationship.RelationshipAttestationState
import io.cordacity.koncerto.contract.relationship.RelationshipState
import io.cordacity.koncerto.workflow.CHECKED_FINALIZING
import io.cordacity.koncerto.workflow.common.CheckedReceiveFinalityFlow
import io.cordacity.koncerto.workflow.currentStep
import net.corda.core.flows.FlowException
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.node.StatesToRecord
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker

class ObserveRelationshipAttestationFlow(
    private val session: FlowSession,
    override val progressTracker: ProgressTracker = tracker()
) : FlowLogic<SignedTransaction>() {

    companion object {
        @JvmStatic
        fun tracker() = ProgressTracker(CHECKED_FINALIZING)
    }

    @Suspendable
    override fun call(): SignedTransaction {
        currentStep(CHECKED_FINALIZING)
        return subFlow(object : CheckedReceiveFinalityFlow(
            session,
            null,
            StatesToRecord.ONLY_RELEVANT,
            CHECKED_FINALIZING.childProgressTracker()
        ) {
            override fun checkTransaction(stx: SignedTransaction) {
                if (stx.tx.outputsOfType<RelationshipAttestationState>().isNotEmpty()) {
                    val relationshipState = serviceHub
                        .toStateAndRef<RelationshipState<*>>(stx.tx.references.single()).state.data

                    if (ourIdentity !in relationshipState.participants) {
                        throw FlowException("Relationship is not relevant to counter-party: $ourIdentity.")
                    }
                }
            }
        })
    }
}