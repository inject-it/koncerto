package io.cordacity.koncerto.workflow.common

import co.paralleluniverse.fibers.Suspendable
import io.cordacity.koncerto.workflow.currentStep
import net.corda.core.crypto.SecureHash
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.flows.ReceiveTransactionFlow
import net.corda.core.node.StatesToRecord
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step

open class CheckedReceiveFinalityFlow(
    private val session: FlowSession,
    private val expectedTransactionId: SecureHash? = null,
    private val statesToRecord: StatesToRecord = StatesToRecord.ONLY_RELEVANT,
    override val progressTracker: ProgressTracker = tracker()
) : FlowLogic<SignedTransaction>() {

    companion object {
        @JvmStatic
        fun tracker() = ProgressTracker(RECEIVING, CHECKING, RECORDING)

        private object RECEIVING : Step("Receiving signed transaction.")
        private object CHECKING : Step("Checking signed transaction.")
        private object RECORDING : Step("Recording signed transaction and states to the vault.")
    }

    @Suspendable
    final override fun call(): SignedTransaction {
        val parentFlow = this
        currentStep(RECEIVING)
        return subFlow(object : ReceiveTransactionFlow(session, true, statesToRecord) {
            override fun checkBeforeRecording(stx: SignedTransaction) {
                parentFlow.currentStep(CHECKING)
                checkTransaction(stx)
                require(expectedTransactionId == null || expectedTransactionId == stx.id) {
                    "We expected to receive transaction with ID $expectedTransactionId but instead got ${stx.id}. " +
                            "Transaction was not recorded and nor its states sent to the vault."
                }
                parentFlow.currentStep(RECORDING)
            }
        })
    }

    open fun checkTransaction(stx: SignedTransaction) = Unit
}