package io.cordacity.koncerto.workflow.revocation

import co.paralleluniverse.fibers.Suspendable
import io.cordacity.koncerto.contract.revocation.RevocationLockContract
import io.cordacity.koncerto.contract.revocation.RevocationLockState
import io.cordacity.koncerto.workflow.*
import net.corda.core.contracts.LinearState
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.flows.StartableByService
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

@StartableByRPC
@StartableByService
class CreateRevocationLockFlow(
    private val linearState: LinearState,
    private val notary: Party? = null,
    override val progressTracker: ProgressTracker = tracker()
) : FlowLogic<SignedTransaction>() {

    companion object {
        @JvmStatic
        fun tracker() = ProgressTracker(GENERATING, VERIFYING, SIGNING, FINALIZING)
    }

    @Suspendable
    override fun call(): SignedTransaction {
        currentStep(GENERATING)
        val transaction = with(TransactionBuilder(notary ?: firstNotary)) {
            addOutputState(RevocationLockState.create(ourIdentity, linearState))
            addCommand(RevocationLockContract.Create, ourIdentity.owningKey)
        }

        currentStep(VERIFYING)
        transaction.verify(serviceHub)

        currentStep(SIGNING)
        val signedTransaction = serviceHub.signInitialTransaction(transaction, ourIdentity.owningKey)

        currentStep(FINALIZING)
        return subFlow(FinalityFlow(signedTransaction, emptyList(), FINALIZING.childProgressTracker()))
    }
}