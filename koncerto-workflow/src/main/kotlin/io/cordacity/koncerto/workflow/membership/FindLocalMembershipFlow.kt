package io.cordacity.koncerto.workflow.membership

import io.cordacity.koncerto.contract.Network
import io.cordacity.koncerto.contract.membership.MembershipSchema.MembershipEntity
import io.cordacity.koncerto.contract.membership.MembershipState
import io.cordacity.koncerto.workflow.QUERYING
import io.cordacity.koncerto.workflow.currentStep
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.StateRef
import net.corda.core.crypto.SecureHash
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.flows.StartableByService
import net.corda.core.identity.AbstractParty
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria.VaultCustomQueryCriteria
import net.corda.core.node.services.vault.QueryCriteria.VaultQueryCriteria
import net.corda.core.node.services.vault.builder
import net.corda.core.utilities.ProgressTracker

@StartableByRPC
@StartableByService
class FindLocalMembershipFlow private constructor(
    private val networkIdentity: AbstractParty,
    private val network: Network,
    private val previousStateRef: StateRef? = null,
    private val status: Vault.StateStatus = Vault.StateStatus.UNCONSUMED,
    override val progressTracker: ProgressTracker = tracker()
) : FlowLogic<List<StateAndRef<MembershipState<*>>>>() {

    constructor(
        networkIdentity: AbstractParty,
        network: Network,
        status: Vault.StateStatus = Vault.StateStatus.UNCONSUMED,
        progressTracker: ProgressTracker = tracker()
    ) : this(networkIdentity, network, null, status, progressTracker)

    constructor(
        state: MembershipState<*>,
        status: Vault.StateStatus = Vault.StateStatus.ALL,
        progressTracker: ProgressTracker = tracker()
    ) : this(state.identity.networkIdentity, state.network, state.previousStateRef, status, progressTracker)

    companion object {
        @JvmStatic
        fun tracker() = ProgressTracker(QUERYING)
    }

    override fun call(): List<StateAndRef<MembershipState<*>>> {
        currentStep(QUERYING)
        return builder {
            val criteria = if (previousStateRef == null) {
                VaultQueryCriteria(status)
                    .and(VaultCustomQueryCriteria(MembershipEntity::networkIdentity.equal(networkIdentity)))
                    .and(VaultCustomQueryCriteria(MembershipEntity::networkHash.equal(network.hash.toString())))
            } else {
                val hash = SecureHash.sha256("${network.hash}$networkIdentity$previousStateRef")
                VaultQueryCriteria(status)
                    .and(VaultCustomQueryCriteria(MembershipEntity::hash.equal(hash)))
            }

            serviceHub.vaultService.queryBy(MembershipState::class.java, criteria)
        }.states
    }
}