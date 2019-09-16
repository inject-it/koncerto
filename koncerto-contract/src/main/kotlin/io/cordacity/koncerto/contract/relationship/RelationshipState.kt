package io.cordacity.koncerto.contract.relationship

import io.cordacity.koncerto.contract.*
import io.cordacity.koncerto.contract.relationship.RelationshipSchema.RelationshipEntity
import io.cordacity.koncerto.contract.relationship.RelationshipSchema.RelationshipSchemaV1
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.StateRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState

/**
 * Represents a configurable multi-lateral network relationship.
 *
 * The configurability of this state allows different business networks to specify requirements for managing
 * multi-lateral working relationships whilst maintaining interoperability with other business networks using
 * the same underlying framework.
 *
 * When creating or amending a relationship state, the transaction must only contain one output, which is the
 * relationship state. This transaction needs to be propagated to all participants of the relationship as all of
 * them are required to attest to the relationship. Relationship attestation is static because any participant of
 * the relationship state can amend it. If attestations were linear, this would pose a vulnerability, as participants
 * would implicitly attest to updated relationship states which could have undesirable consequences on the network.
 *
 * The network operator is considered a participant of this state because they have authority to create, amend
 * or even revoke relationships as they see fit, however relationships are still considered invalid until attested.
 *
 * @property network The identity of the network.
 * @property configuration The configuration which determines how participants of the relationship cooperate.
 * @property previousStateRef The state ref to the previous version of the state, or null if this is this first version.
 * @property linearId The unique identifier of the state.
 * @property participants The relationship configuration network identities, and optionally the network operator.
 * @property hash A SHA-256 hash that uniquely identifies this version of the state.
 */
@BelongsToContract(RelationshipContract::class)
data class RelationshipState<T : Configuration>(
    override val network: Network,
    val configuration: T,
    val previousStateRef: StateRef? = null,
    override val linearId: UniqueIdentifier = UniqueIdentifier()
) : NetworkState(), Hashable {

    override val hash: SecureHash
        get() = SecureHash.sha256("${network.hash}${configuration.hash}$previousStateRef")

    override val participants: List<AbstractParty>
        get() = (configuration.networkIdentities + network.operator).filterNotNull()

    /**
     * Maps this state to a persistent state.
     */
    override fun generateMappedObject(schema: MappedSchema): PersistentState = when (schema) {
        is RelationshipSchemaV1 -> RelationshipEntity(
            linearId = linearId.id,
            externalId = linearId.externalId,
            networkName = network.name,
            normalizedNetworkName = network.normalizedName,
            networkOperator = network.operator,
            networkHash = network.hash.toString(),
            identityHash = configuration.networkIdentities.identityHash.toString(),
            hash = hash.toString()
        )
        else -> throw IllegalArgumentException("Unrecognised schema: $schema.")
    }

    /**
     * Gets a list of supported state schemas.
     */
    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(RelationshipSchemaV1)
}