package io.cordacity.koncerto.workflow.membership

import io.cordacity.koncerto.contract.Network
import io.cordacity.koncerto.contract.membership.MembershipAttestationSchema.MembershipAttestationEntity
import io.cordacity.koncerto.contract.membership.MembershipAttestationState
import io.cordacity.koncerto.contract.membership.MembershipState
import io.cordacity.koncerto.workflow.QUERYING
import io.cordacity.koncerto.workflow.currentStep
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.StateRef
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
class FindLocalMembershipAttestationFlow(
    private val attestee: AbstractParty,
    private val network: Network,
    private val stateRef: StateRef? = null,
    private val status: Vault.StateStatus = Vault.StateStatus.UNCONSUMED,
    override val progressTracker: ProgressTracker = tracker()
) : FlowLogic<List<StateAndRef<MembershipAttestationState>>>() {

    constructor(
        membership: StateAndRef<MembershipState<*>>,
        progressTracker: ProgressTracker = tracker()
    ) : this(
        membership.state.data.identity.networkIdentity,
        membership.state.data.network,
        membership.ref,
        Vault.StateStatus.ALL,
        progressTracker
    )

    constructor(
        membership: MembershipState<*>,
        status: Vault.StateStatus = Vault.StateStatus.UNCONSUMED,
        progressTracker: ProgressTracker = tracker()
    ) : this(
        membership.identity.networkIdentity,
        membership.network,
        null,
        status,
        progressTracker
    )

    companion object {
        @JvmStatic
        fun tracker() = ProgressTracker(QUERYING)
    }

    override fun call(): List<StateAndRef<MembershipAttestationState>> {
        currentStep(QUERYING)
        return builder {
            val criteria = if (stateRef == null) {
                VaultQueryCriteria(status)
                    .and(VaultCustomQueryCriteria(MembershipAttestationEntity::attestee.equal(attestee)))
                    .and(VaultCustomQueryCriteria(MembershipAttestationEntity::networkHash.equal(network.hash)))
            } else {
                val hash = stateRef.txhash.toString()
                val index = stateRef.index
                VaultQueryCriteria(status)
                    .and(VaultCustomQueryCriteria(MembershipAttestationEntity::attestee.equal(attestee)))
                    .and(VaultCustomQueryCriteria(MembershipAttestationEntity::networkHash.equal(network.hash)))
                    .and(VaultCustomQueryCriteria(MembershipAttestationEntity::membershipStateRefHash.equal(hash)))
                    .and(VaultCustomQueryCriteria(MembershipAttestationEntity::membershipStateRefIndex.equal(index)))
            }

            serviceHub.vaultService.queryBy(MembershipAttestationState::class.java, criteria)
        }.states
    }
}