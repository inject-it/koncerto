package io.cordacity.koncerto.workflow.membership

import io.cordacity.koncerto.contract.Network
import io.cordacity.koncerto.contract.membership.MembershipAttestationSchema
import io.cordacity.koncerto.contract.membership.MembershipAttestationSchema.MembershipAttestationEntity
import net.corda.core.contracts.StateRef
import net.corda.core.identity.AbstractParty
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.QueryCriteria.VaultCustomQueryCriteria
import net.corda.core.node.services.vault.QueryCriteria.VaultQueryCriteria
import net.corda.core.node.services.vault.builder

fun MembershipAttestationSchema.getQueryCriteria(
    network: Network,
    status: Vault.StateStatus = Vault.StateStatus.UNCONSUMED
): QueryCriteria = builder {
    return VaultQueryCriteria(status)
        .and(VaultCustomQueryCriteria(MembershipAttestationEntity::networkHash.equal(network.hash.toString())))
}

fun MembershipAttestationSchema.getQueryCriteria(
    network: Network,
    attestee: AbstractParty,
    membershipStateRef: StateRef? = null,
    status: Vault.StateStatus = Vault.StateStatus.UNCONSUMED
): QueryCriteria = builder {
    return if (membershipStateRef == null) {
        VaultQueryCriteria(status)
            .and(VaultCustomQueryCriteria(MembershipAttestationEntity::networkHash.equal(network.hash.toString())))
            .and(VaultCustomQueryCriteria(MembershipAttestationEntity::attestee.equal(attestee)))
    } else {
        val hash = membershipStateRef.txhash.toString()
        val index = membershipStateRef.index
        VaultQueryCriteria(status)
            .and(VaultCustomQueryCriteria(MembershipAttestationEntity::networkHash.equal(network.hash.toString())))
            .and(VaultCustomQueryCriteria(MembershipAttestationEntity::attestee.equal(attestee)))
            .and(VaultCustomQueryCriteria(MembershipAttestationEntity::membershipStateRefHash.equal(hash)))
            .and(VaultCustomQueryCriteria(MembershipAttestationEntity::membershipStateRefIndex.equal(index)))
    }
}