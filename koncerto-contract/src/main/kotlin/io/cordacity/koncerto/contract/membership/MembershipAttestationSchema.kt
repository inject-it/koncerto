package io.cordacity.koncerto.contract.membership

import io.cordacity.koncerto.contract.AttestationStatus
import net.corda.core.crypto.NullKeys.NULL_PARTY
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import java.util.*
import javax.persistence.*

object MembershipAttestationSchema {

    @Entity
    @Table(name = "membership_attestation_states")
    class MembershipAttestationEntity(
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

        @Column(name = "membership_linear_id", nullable = false)
        val membershipLinearId: UUID = UUID.randomUUID(),

        @Column(name = "membership_external_id", nullable = true)
        val membershipExternalId: String? = null,

        @Column(name = "membership_stateref_hash", nullable = false)
        val membershipStateRefHash: String = "",

        @Column(name = "membership_stateref_index", nullable = false)
        val membershipStateRefIndex: Int = 0,

        @Column(name = "attestor", nullable = false)
        val attestor: AbstractParty = NULL_PARTY,

        @Column(name = "attestee", nullable = false)
        val attestee: AbstractParty = NULL_PARTY,

        @Column(name = "status", nullable = false)
        @Enumerated(EnumType.STRING)
        val status: AttestationStatus = AttestationStatus.REJECTED
    ) : PersistentState()

    private const val SCHEMA_VERSION_1 = 1

    object MembershipAttestationSchemaV1 : MappedSchema(
        schemaFamily = MembershipSchema.javaClass,
        version = SCHEMA_VERSION_1,
        mappedTypes = listOf(MembershipAttestationEntity::class.java)
    )
}