package io.cordacity.koncerto.workflow.membership

import co.paralleluniverse.fibers.Suspendable
import io.cordacity.koncerto.workflow.CHECKED_FINALIZING
import io.cordacity.koncerto.workflow.common.CheckedReceiveFinalityFlow
import io.cordacity.koncerto.workflow.currentStep
import net.corda.core.crypto.SecureHash
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.node.StatesToRecord
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker

class ObserveMembershipFlow(
    private val session: FlowSession,
    private val expectedTransactionId: SecureHash? = null,
    override val progressTracker: ProgressTracker = tracker()
) : FlowLogic<SignedTransaction>() {

    companion object {
        @JvmStatic
        fun tracker() = ProgressTracker(CHECKED_FINALIZING)
    }

    @Suspendable
    override fun call(): SignedTransaction {
        currentStep(CHECKED_FINALIZING)
        return subFlow(
            CheckedReceiveFinalityFlow(
                session,
                expectedTransactionId,
                StatesToRecord.ALL_VISIBLE,
                CHECKED_FINALIZING.childProgressTracker()
            )
        )
    }
}