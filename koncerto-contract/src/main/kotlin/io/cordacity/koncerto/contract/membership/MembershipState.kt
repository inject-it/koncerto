package io.cordacity.koncerto.contract.membership

import io.cordacity.koncerto.contract.*
import io.cordacity.koncerto.contract.membership.MembershipSchema.MembershipEntity
import io.cordacity.koncerto.contract.membership.MembershipSchema.MembershipSchemaV1
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.StateRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState

/**
 * Represents a configurable network membership.
 *
 * The configurability of this state allows different business networks to specify requirements for managing legal
 * identity whilst maintaining interoperability with other business networks using the same underlying framework.
 *
 * This can be very useful for handling KYC checks through legal identity information and attachments and ensuring
 * that network members are appropriately authorised through roles and claims.
 *
 * When creating or amending a membership state, the transaction must only contain one output, which is the membership
 * state. This transaction needs to be propagated to all participants from which the member requires attestation.
 * Membership attestation is static because a member can update their own membership state. If attestations were
 * linear, this would pose a vulnerability, as participants would implicitly attest to updated membership states which
 * could have undesirable consequences on the network.
 *
 * @property network The identity of the network that the membership is bound to.
 * @property identity The configurable identity of the network member.
 * @property roles The roles that are possessed by the network member.
 * @property claims The claims that are possessed by the network member.
 * @property attachments The attachments that are possessed by the network member.
 * @property previousStateRef The state ref to the previous version of the state, or null if this is this first version.
 * @property linearId The unique identifier of the membership state.
 * @property participants The network identity of the network member, and optionally the network operator.
 * @property isNetworkOperator Determines whether the network member is the network operator.
 * @property hash A SHA-256 hash that uniquely identifies this version of the state.
 */
@BelongsToContract(MembershipContract::class)
data class MembershipState<T : Identity>(
    override val network: Network,
    val identity: T,
    val roles: Set<Role> = emptySet(),
    val claims: Set<Claim> = emptySet(),
    val attachments: Set<SecureHash> = emptySet(),
    val previousStateRef: StateRef? = null,
    override val linearId: UniqueIdentifier = UniqueIdentifier()
) : NetworkState(), Hashable {

    override val participants: List<AbstractParty>
        get() = listOfNotNull(identity.networkIdentity, network.operator)

    val isNetworkOperator: Boolean
        get() = identity.networkIdentity == network.operator

    override val hash: SecureHash
        get() = SecureHash.sha256("${network.hash}${identity.networkIdentity}$previousStateRef")

    /**
     * Maps this state to a persistent state.
     */
    override fun generateMappedObject(schema: MappedSchema): PersistentState = when (schema) {
        is MembershipSchemaV1 -> MembershipEntity(
            linearId = linearId.id,
            externalId = linearId.externalId,
            networkName = network.name,
            normalizedNetworkName = network.normalizedName,
            networkOperator = network.operator,
            networkIdentity = identity.networkIdentity,
            networkHash = network.hash.toString(),
            isNetworkOperator = isNetworkOperator,
            hash = hash.toString()
        )
        else -> throw IllegalArgumentException("Unrecognised schema: $schema.")
    }

    /**
     * Gets a list of supported state schemas.
     */
    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(MembershipSchemaV1)

    /**
     * Determines whether the network member possesses a specific role.
     *
     * @param roleName The role name to check in this membership.
     * @return Returns true if the network member possesses a specific role; otherwise, false.
     */
    fun hasRole(roleName: String): Boolean = roleName.toLowerCase() in roles.map { it.normalizedName }

    /**
     * Determines whether the network member possesses a specific role.
     *
     * @param role The role to check in this membership.
     * @return Returns true if the network member possesses a specific role; otherwise, false.
     */
    fun hasRole(role: Role): Boolean = role in roles

    /**
     * Adds the specified roles to the network member's membership.
     *
     * @param roleNames The role names to add to the network member's membership.
     * @return Returns a new membership with the specified roles added.
     */
    fun addRoles(vararg roleNames: String) = copy(roles = roles + roleNames.map { Role(it) })

    /**
     * Adds the specified roles to the network member's membership.
     *
     * @param roles The roles to add to the network member's membership.
     * @return Returns a new membership with the specified roles added.
     */
    fun addRoles(vararg roles: Role) = copy(roles = this.roles + roles)

    /**
     * Removes the specified roles to the network member's membership.
     *
     * @param roleNames The role names to remove from the network member's membership.
     * @return Returns a new membership with the specified roles removed.
     */
    fun removeRoles(vararg roleNames: String) = copy(roles = roles - roleNames.map { Role(it) })

    /**
     * Removes the specified roles to the network member's membership.
     *
     * @param roles The roles to remove from the network member's membership.
     * @return Returns a new membership with the specified roles removed.
     */
    fun removeRoles(vararg roles: Role) = copy(roles = this.roles - roles)

    /**
     * Gets a claim from this membership, or null if no claim exists with the specified key.
     *
     * @param key The key of the claim from which to obtain a value.
     * @return Returns a claim from this membership, or null if no claim exists with the specified key.
     */
    fun getClaim(key: String): Claim? = claims.singleOrNull { it.normalizedKey == key.toLowerCase() }

    /**
     * Determines whether the network member possesses a specific claim.
     *
     * @param key The key of the claim to check in this membership.
     * @return Returns true if the network member possesses a specific claim; otherwise, false.
     */
    fun hasClaim(key: String) = getClaim(key) != null

    /**
     * Adds the specified claim to the network member's membership.
     *
     * @param key The key of the claim to add.
     * @param value The value of the claim to add.
     * @return Returns a new membership with the specified claim added.
     */
    fun addClaim(key: String, value: String) = copy(claims = claims + Claim(key, value))

    /**
     * Removes the specified claim to the network member's membership.
     *
     * @param key The key of the claim to remove.
     * @return Returns a new membership with the specified claim removed.
     */
    fun removeClaim(key: String) = if (getClaim(key) != null) copy(claims = claims - getClaim(key)!!) else this

    /**
     * Determines whether the network member possesses a specific attachment.
     *
     * @param attachment The hash of the attachment to check in this membership.
     * @return Returns true if the network member possesses a specific attachment; otherwise, false.
     */
    fun hasAttachment(attachment: SecureHash) = attachment in attachments

    /**
     * Adds the specified attachments to the network member's membership.
     *
     * @param attachments The hashes of the attachments to add.
     * @return Returns a new membership with the specified attachments added.
     */
    fun addAttactments(vararg attachments: SecureHash) = copy(attachments = this.attachments + attachments)

    /**
     * Removes the specified attachments to the network member's membership.
     *
     * @param attachments The hashes of the attachments to remove.
     * @return Returns a new membership with the specified attachments remove.
     */
    fun removeAttachments(vararg attachments: SecureHash) = copy(attachments = this.attachments - attachments)
}