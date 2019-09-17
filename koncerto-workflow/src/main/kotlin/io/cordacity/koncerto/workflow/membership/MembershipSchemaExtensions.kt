package io.cordacity.koncerto.workflow.membership

import io.cordacity.koncerto.contract.Network
import io.cordacity.koncerto.contract.membership.MembershipSchema
import io.cordacity.koncerto.contract.membership.MembershipSchema.MembershipEntity
import net.corda.core.contracts.StateRef
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.AbstractParty
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.QueryCriteria.VaultCustomQueryCriteria
import net.corda.core.node.services.vault.QueryCriteria.VaultQueryCriteria
import net.corda.core.node.services.vault.builder

fun MembershipSchema.getQueryCriteria(
    network: Network,
    status: Vault.StateStatus = Vault.StateStatus.UNCONSUMED
): QueryCriteria = builder {
    return VaultQueryCriteria(status)
        .and(VaultCustomQueryCriteria(MembershipEntity::networkHash.equal(network.hash.toString())))
}

fun MembershipSchema.getQueryCriteria(
    network: Network,
    networkIdentity: AbstractParty,
    status: Vault.StateStatus = Vault.StateStatus.UNCONSUMED
): QueryCriteria = builder {
    return VaultQueryCriteria(status)
        .and(VaultCustomQueryCriteria(MembershipEntity::networkHash.equal(network.hash.toString())))
        .and(VaultCustomQueryCriteria(MembershipEntity::networkIdentity.equal(networkIdentity)))
}

fun MembershipSchema.getQueryCriteria(
    network: Network,
    networkIdentity: AbstractParty,
    previousStateRef: StateRef,
    status: Vault.StateStatus = Vault.StateStatus.UNCONSUMED
): QueryCriteria = builder {
    val hash = SecureHash.sha256("${network.hash}$networkIdentity$previousStateRef")
    return VaultQueryCriteria(status)
        .and(VaultCustomQueryCriteria(MembershipEntity::hash.equal(hash.toString())))
}