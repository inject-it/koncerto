package io.cordacity.koncerto.contract.relationship

import io.cordacity.koncerto.contract.*
import io.cordacity.koncerto.contract.relationship.RelationshipAttestationSchema.RelationshipAttestationEntity
import io.cordacity.koncerto.contract.relationship.RelationshipAttestationSchema.RelationshipAttestationSchemaV1
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState

/**
 * Represents an attestation that points to a specific [RelationshipState] version.
 *
 * @property network The identity of the network that the relationship attestation is bound to.
 * @property pointer A pointer to the relationship state being attested.
 * @property attestor The participant attesting to the attested relationship state.
 * @property attestees A set of participants for whom the relationship state is being attested.
 * @property status Specifies whether the attestation is accepted or rejected.
 * @property metadata Allows additional information to be added to the attestation for reference.
 * @property externalId The external identity of the attestation, or null if no external identity is required.
 * @property linearId The unique identifier of the relationship attestation state.
 * @property participants The participants of the attestation state, namely the attestor, attestees and network operator.
 */
@BelongsToContract(RelationshipAttestationContract::class)
data class RelationshipAttestationState(
    override val network: Network,
    override val pointer: AttestationPointer<RelationshipState<*>>,
    override val attestor: AbstractParty,
    override val attestees: Set<AbstractParty>,
    override val status: AttestationStatus = AttestationStatus.REJECTED,
    override val metadata: Map<String, String> = emptyMap(),
    val externalId: String? = null
) : AttestationState<RelationshipState<*>>() {

    companion object {

        /**
         * Creates a relationship attestation.
         *
         * @param attestor The participant attesting to the attested relationship state.
         * @param relationship The relationship state being attested.
         * @param status Specifies whether the attestation is accepted or rejected.
         * @param metadata Allows additional information to be added to the attestation for reference.
         * @param externalId The external identity of the attestation, or null if no external identity is required.
         * @return Returns a relationship attestation.
         */
        fun create(
            attestor: AbstractParty,
            relationship: StateAndRef<RelationshipState<*>>,
            status: AttestationStatus = AttestationStatus.REJECTED,
            metadata: Map<String, String> = emptyMap(),
            externalId: String? = null
        ) = RelationshipAttestationState(
            network = relationship.state.data.network,
            pointer = AttestationPointer.create(relationship),
            attestor = attestor,
            attestees = (relationship.state.data.participants - attestor).toSet(),
            status = status,
            metadata = metadata,
            externalId = externalId
        )
    }

    override val linearId: UniqueIdentifier
        get() = HashUtils.createRelationshipAttestationIdentifier(
            network,
            attestor,
            pointer.linearId.id,
            externalId
        )

    /**
     * Creates an accepted attestation.
     *
     * @param stateAndRef The [StateAndRef] being attested, or the current state if the [StateAndRef] is null.
     * @param metadata Allows additional information to be added to the attestation for reference.
     * @return Returns an accepted attestation.
     */
    override fun accept(stateAndRef: StateAndRef<RelationshipState<*>>?, metadata: Map<String, String>) = copy(
        pointer = if (stateAndRef != null) AttestationPointer.create(stateAndRef) else pointer,
        metadata = metadata,
        status = AttestationStatus.ACCEPTED
    )

    /**
     * Creates an rejected attestation.
     *
     * @param stateAndRef The [StateAndRef] being attested, or the current state if the [StateAndRef] is null.
     * @param metadata Allows additional information to be added to the attestation for reference.
     * @return Returns an rejected attestation.
     */
    override fun reject(stateAndRef: StateAndRef<RelationshipState<*>>?, metadata: Map<String, String>) = copy(
        pointer = if (stateAndRef != null) AttestationPointer.create(stateAndRef) else pointer,
        metadata = metadata,
        status = AttestationStatus.REJECTED
    )

    /**
     * Maps this state to a persistent state.
     */
    override fun generateMappedObject(schema: MappedSchema): PersistentState = when (schema) {
        is RelationshipAttestationSchemaV1 -> RelationshipAttestationEntity(
            linearId = linearId.id,
            externalId = linearId.externalId,
            networkName = network.name,
            normalizedNetworkName = network.normalizedName,
            networkOperator = network.operator,
            networkHash = network.hash.toString(),
            participantHash = HashUtils.createParticipantsHash(participants.toSet()).toString(),
            relationshipLinearId = pointer.linearId.id,
            relationshipExternalId = pointer.linearId.externalId,
            relationshipStateRefHash = pointer.stateRef.txhash.toString(),
            relationshipStateRefIndex = pointer.stateRef.index,
            attestor = attestor,
            status = status
        )
        else -> throw IllegalArgumentException("Unrecognised schema: $schema.")
    }

    /**
     * Gets a list of supported state schemas.
     */
    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(RelationshipAttestationSchemaV1)
}