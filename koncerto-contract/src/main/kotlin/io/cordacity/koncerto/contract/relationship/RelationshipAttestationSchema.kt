package io.cordacity.koncerto.contract.relationship

import io.cordacity.koncerto.contract.AttestationStatus
import net.corda.core.crypto.NullKeys.NULL_PARTY
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import java.util.*
import javax.persistence.*

object RelationshipAttestationSchema {

    @Entity
    @Table(name = "relationship_attestation_states")
    class RelationshipAttestationEntity(
        @Column(name = "linear_id", nullable = false)
        val linearId: UUID = UUID.randomUUID(),

        @Column(name = "external_id", nullable = true)
        val externalId: String? = null,

        @Column(name = "network_name", nullable = false)
        val networkName: String = "",

        @Column(name = "normalized_network_name", nullable = false)
        val normalizedNetworkName: String = "",

        @Column(name = "network_operator", nullable = true)
        val networkOperator: AbstractParty? = null,

        @Column(name = "network_hash", nullable = false)
        val networkHash: String = "",

        @Column(name = "participant_hash", nullable = false)
        val participantHash: String = "",

        @Column(name = "relationship_linear_id", nullable = false)
        val relationshipLinearId: UUID = UUID.randomUUID(),

        @Column(name = "relationship_external_id", nullable = true)
        val relationshipExternalId: String? = null,

        @Column(name = "relationship_stateref_hash", nullable = false)
        val relationshipStateRefHash: String = "",

        @Column(name = "relationship_stateref_index", nullable = false)
        val relationshipStateRefIndex: Int = 0,

        @Column(name = "attestor", nullable = false)
        val attestor: AbstractParty = NULL_PARTY,

        @Column(name = "status", nullable = false)
        @Enumerated(EnumType.STRING)
        val status: AttestationStatus = AttestationStatus.REJECTED
    ) : PersistentState()

    private const val SCHEMA_VERSION_1 = 1

    object RelationshipAttestationSchemaV1 : MappedSchema(
        schemaFamily = RelationshipAttestationSchema.javaClass,
        version = SCHEMA_VERSION_1,
        mappedTypes = listOf(RelationshipAttestationEntity::class.java)
    )
}