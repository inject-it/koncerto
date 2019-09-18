package io.cordacity.koncerto.workflow.relationship

import co.paralleluniverse.fibers.Suspendable
import io.cordacity.koncerto.contract.relationship.RelationshipState
import io.cordacity.koncerto.contract.revocation.RevocationLockState
import io.cordacity.koncerto.contract.revocation.RevocationLockStatus
import io.cordacity.koncerto.workflow.*
import io.cordacity.koncerto.workflow.common.CheckedReceiveFinalityFlow
import io.cordacity.koncerto.workflow.revocation.DeleteRevocationLockFlow
import io.cordacity.koncerto.workflow.revocation.FindLocalRevocationLockFlow
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.FlowException
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.flows.SignTransactionFlow
import net.corda.core.node.StatesToRecord
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step
import net.corda.core.utilities.unwrap

class ObserveRelationshipFlow(
    private val session: FlowSession,
    override val progressTracker: ProgressTracker = tracker()
) : FlowLogic<SignedTransaction>() {

    companion object {
        @JvmStatic
        fun tracker() = ProgressTracker(RECEIVING, COUNTERSIGNING, CHECKED_FINALIZING, DELETING)

        private object RECEIVING : Step("Receiving flow action instructions.")
        private object DELETING : Step("Deleting revocation lock.")
    }

    private var revocationLock: StateAndRef<RevocationLockState<*>>? = null

    @Suspendable
    override fun call(): SignedTransaction {
        currentStep(RECEIVING)
        val (flowAction, checkMembership) = session.receive<Pair<FlowAction, Boolean>>().unwrap { it }

        currentStep(COUNTERSIGNING)
        val transaction = subFlow(object : SignTransactionFlow(session) {
            override fun checkTransaction(stx: SignedTransaction) {
                if ((flowAction == FlowAction.ISSUING || flowAction == FlowAction.AMENDING) && checkMembership) {
                    val relationship = stx.tx.outputsOfType<RelationshipState<*>>().singleOrNull()
                        ?: throw FlowException("Failed to obtain a single relationship state from the transaction.")
                    checkMembershipsAndAttestations(relationship.participants - ourIdentity, relationship.network)
                }

                if (flowAction == FlowAction.REVOKING) {
                    val stateRef = stx.tx.inputs.singleOrNull()
                        ?: throw FlowException("Failed to obtain a single state reference from the transaction.")
                    val relationship = serviceHub.toStateAndRef<RelationshipState<*>>(stateRef)
                    revocationLock = subFlow(FindLocalRevocationLockFlow(relationship.state.data))
                    if (revocationLock?.state?.data?.status == RevocationLockStatus.LOCKED) {
                        throw FlowException("Revocation of this relationship is locked by counter-party: $ourIdentity")
                    }
                }
            }
        })

        currentStep(CHECKED_FINALIZING)
        val signedTransaction = subFlow(
            CheckedReceiveFinalityFlow(
                session,
                transaction.id,
                StatesToRecord.ONLY_RELEVANT,
                CHECKED_FINALIZING.childProgressTracker()
            )
        )

        currentStep(DELETING)
        if (revocationLock != null) {
            subFlow(DeleteRevocationLockFlow(revocationLock!!))
        }

        return signedTransaction
    }
}