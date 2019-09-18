package io.cordacity.koncerto.workflow.revocation

import io.cordacity.koncerto.contract.revocation.RevocationLockSchema
import io.cordacity.koncerto.contract.revocation.RevocationLockSchema.RevocationLockEntity
import net.corda.core.contracts.LinearState
import net.corda.core.identity.AbstractParty
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.QueryCriteria.VaultCustomQueryCriteria
import net.corda.core.node.services.vault.QueryCriteria.VaultQueryCriteria
import net.corda.core.node.services.vault.builder

fun RevocationLockSchema.getQueryCriteria(
    owner: AbstractParty,
    linearState: LinearState,
    status: Vault.StateStatus = Vault.StateStatus.UNCONSUMED
): QueryCriteria = builder {
    val id = linearState.linearId.id
    val canonicalName = linearState.javaClass.canonicalName
    return VaultQueryCriteria(status)
        .and(VaultCustomQueryCriteria(RevocationLockEntity::owner.equal(owner)))
        .and(VaultCustomQueryCriteria(RevocationLockEntity::linearId.equal(id)))
        .and(VaultCustomQueryCriteria(RevocationLockEntity::canonicalName.equal(canonicalName)))
}