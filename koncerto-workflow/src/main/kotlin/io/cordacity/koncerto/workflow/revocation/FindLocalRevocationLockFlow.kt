package io.cordacity.koncerto.workflow.revocation

import io.cordacity.koncerto.contract.revocation.RevocationLockSchema
import io.cordacity.koncerto.contract.revocation.RevocationLockState
import io.cordacity.koncerto.workflow.QUERYING
import io.cordacity.koncerto.workflow.currentStep
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.flows.StartableByService
import net.corda.core.node.services.queryBy
import net.corda.core.utilities.ProgressTracker

@StartableByRPC
@StartableByService
class FindLocalRevocationLockFlow(
    private val linearState: LinearState,
    override val progressTracker: ProgressTracker = tracker()
) : FlowLogic<StateAndRef<RevocationLockState<*>>?>() {

    companion object {
        @JvmStatic
        fun tracker() = ProgressTracker(QUERYING)
    }

    override fun call(): StateAndRef<RevocationLockState<*>>? {
        currentStep(QUERYING)
        val criteria = RevocationLockSchema.getQueryCriteria(ourIdentity, linearState)
        return serviceHub.vaultService.queryBy<RevocationLockState<*>>(criteria).states.singleOrNull()
    }
}