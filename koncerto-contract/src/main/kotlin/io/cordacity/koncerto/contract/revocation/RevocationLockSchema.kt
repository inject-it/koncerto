package io.cordacity.koncerto.contract.revocation

import net.corda.core.crypto.NullKeys.NULL_PARTY
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import java.util.*
import javax.persistence.*

object RevocationLockSchema {

    @Entity
    @Table(name = "revocation_lock_states")
    class RevocationLockEntity(
        @Column(name = "owner", nullable = false)
        val owner: AbstractParty = NULL_PARTY,

        @Column(name = "linearId", nullable = false)
        val linearId: UUID = UUID.randomUUID(),

        @Column(name = "externalId", nullable = true)
        val externalId: String? = null,

        @Column(name = "canonical_name", nullable = false)
        val canonicalName: String = "",

        @Column(name = "status", nullable = false)
        @Enumerated(EnumType.STRING)
        val status: RevocationLockStatus = RevocationLockStatus.LOCKED
    ) : PersistentState()

    private const val SCHEMA_VERSION_1 = 1

    object RevocationLockSchemaV1 : MappedSchema(
        schemaFamily = RevocationLockSchema.javaClass,
        version = SCHEMA_VERSION_1,
        mappedTypes = listOf(RevocationLockEntity::class.java)
    )
}