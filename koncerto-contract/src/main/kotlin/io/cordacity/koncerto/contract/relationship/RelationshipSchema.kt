package io.cordacity.koncerto.contract.relationship

import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

object RelationshipSchema {

    @Entity
    @Table(name = "relationship_states")
    class RelationshipEntity(
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
        val participantHash: String = ""
    ) : PersistentState()

    private const val SCHEMA_VERSION_1 = 1

    object RelationshipSchemaV1 : MappedSchema(
        schemaFamily = RelationshipSchema.javaClass,
        version = SCHEMA_VERSION_1,
        mappedTypes = listOf(RelationshipEntity::class.java)
    )
}