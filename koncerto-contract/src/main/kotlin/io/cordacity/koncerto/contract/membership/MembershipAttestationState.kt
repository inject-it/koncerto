package io.cordacity.koncerto.contract.membership

import io.cordacity.koncerto.contract.AttestationPointer
import io.cordacity.koncerto.contract.AttestationState
import io.cordacity.koncerto.contract.AttestationStatus
import io.cordacity.koncerto.contract.Network
import io.cordacity.koncerto.contract.membership.MembershipAttestationSchema.MembershipAttestationEntity
import io.cordacity.koncerto.contract.membership.MembershipAttestationSchema.MembershipAttestationSchemaV1
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState

/**
 * Represents an attestation that points to a specific [MembershipState] version.
 *
 * @property network The identity of the network that the membership attestation is bound to.
 * @property pointer A pointer to the membership state being attested.
 * @property attestor The participant attesting to the attested membership state.
 * @property attestees A set of participants for whom the membership state is being attested.
 * @property status Specifies whether the attestation is accepted or rejected.
 * @property metadata Allows additional information to be added to the attestation for reference.
 * @property linearId The unique identifier of the membership attestation state.
 * @property participants The participants of the attestation state, namely the attestor, attestees and network operator.
 */
@BelongsToContract(MembershipAttestationContract::class)
data class MembershipAttestationState(
    override val network: Network,
    override val pointer: AttestationPointer<MembershipState<*>>,
    override val attestor: AbstractParty,
    override val attestees: Set<AbstractParty>,
    override val status: AttestationStatus = AttestationStatus.REJECTED,
    override val metadata: Map<String, String> = emptyMap(),
    override val linearId: UniqueIdentifier = UniqueIdentifier()
) : AttestationState<MembershipState<*>>() {

    companion object {

        /**
         * Creates a membership attestation.
         *
         * @param attestor The participant attesting to the attested membership state.
         * @param membership The membership state being attested.
         * @param status Specifies whether the attestation is accepted or rejected.
         * @param metadata Allows additional information to be added to the attestation for reference.
         * @param linearId The unique identifier of the membership attestation state.
         * @return Returns a membership attestation.
         */
        fun create(
            attestor: AbstractParty,
            membership: StateAndRef<MembershipState<*>>,
            status: AttestationStatus = AttestationStatus.REJECTED,
            metadata: Map<String, String> = emptyMap(),
            linearId: UniqueIdentifier = UniqueIdentifier()
        ) = MembershipAttestationState(
            network = membership.state.data.network,
            pointer = AttestationPointer.create(membership),
            attestor = attestor,
            attestees = setOf(membership.state.data.identity.networkIdentity),
            status = status,
            metadata = metadata,
            linearId = linearId
        )
    }

    init {
        check(attestees.size == 1) { "There can only be one attestee for a membership attestation state." }
    }

    val attestee: AbstractParty
        get() = attestees.single()

    /**
     * Creates an accepted attestation.
     *
     * @param stateAndRef The [StateAndRef] being attested, or the current state if the [StateAndRef] is null.
     * @param metadata Allows additional information to be added to the attestation for reference.
     * @return Returns an accepted attestation.
     */
    override fun accept(stateAndRef: StateAndRef<MembershipState<*>>?, metadata: Map<String, String>) = copy(
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
    override fun reject(stateAndRef: StateAndRef<MembershipState<*>>?, metadata: Map<String, String>) = copy(
        pointer = if (stateAndRef != null) AttestationPointer.create(stateAndRef) else pointer,
        metadata = metadata,
        status = AttestationStatus.REJECTED
    )

    /**
     * Maps this state to a persistent state.
     */
    override fun generateMappedObject(schema: MappedSchema): PersistentState = when (schema) {
        is MembershipAttestationSchemaV1 -> MembershipAttestationEntity(
            linearId = linearId.id,
            externalId = linearId.externalId,
            networkName = network.name,
            normalizedNetworkName = network.normalizedName,
            networkOperator = network.operator,
            networkHash = network.hash.toString(),
            membershipLinearId = pointer.linearId.id,
            membershipExternalId = pointer.linearId.externalId,
            membershipStateRefHash = pointer.stateRef.txhash.toString(),
            membershipStateRefIndex = pointer.stateRef.index,
            attestor = attestor,
            attestee = attestees.single(),
            status = status
        )
        else -> throw IllegalArgumentException("Unrecognised schema: $schema.")
    }

    /**
     * Gets a list of supported state schemas.
     */
    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(MembershipAttestationSchemaV1)
}