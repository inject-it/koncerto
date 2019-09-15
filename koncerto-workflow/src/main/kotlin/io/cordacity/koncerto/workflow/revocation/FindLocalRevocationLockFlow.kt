package io.cordacity.koncerto.workflow.revocation

import io.cordacity.koncerto.contract.revocation.RevocationLockSchema.RevocationLockEntity
import io.cordacity.koncerto.contract.revocation.RevocationLockState
import io.cordacity.koncerto.workflow.QUERYING
import io.cordacity.koncerto.workflow.currentStep
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.flows.StartableByService
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria.VaultCustomQueryCriteria
import net.corda.core.node.services.vault.QueryCriteria.VaultQueryCriteria
import net.corda.core.node.services.vault.builder
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
        return builder {
            val id = linearState.linearId.id
            val canonicalName = linearState.javaClass.canonicalName
            val criteria = VaultQueryCriteria(Vault.StateStatus.UNCONSUMED)
                .and(VaultCustomQueryCriteria(RevocationLockEntity::owner.equal(ourIdentity)))
                .and(VaultCustomQueryCriteria(RevocationLockEntity::linearId.equal(id)))
                .and(VaultCustomQueryCriteria(RevocationLockEntity::canonicalName.equal(canonicalName)))

            serviceHub.vaultService.queryBy(RevocationLockState::class.java, criteria)
        }.states.singleOrNull()
    }
}