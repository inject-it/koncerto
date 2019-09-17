package io.cordacity.koncerto.contract.revocation

import io.cordacity.koncerto.contract.revocation.RevocationLockSchema.RevocationLockEntity
import io.cordacity.koncerto.contract.revocation.RevocationLockSchema.RevocationLockSchemaV1
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.CommandAndState
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.OwnableState
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState

/**
 * Represents a revocation lock that prevents revocation of a state.
 *
 * @property owner The owner of the revocation lock.
 * @property pointer A pointer to the state that requires a revocation lock.
 * @property status Specifies whether the revocation lock is locked or unlocked.
 * @property participants The participants of the revocation lock, which is only the owner.
 */
@BelongsToContract(RevocationLockContract::class)
data class RevocationLockState<T : LinearState>(
    override val owner: AbstractParty,
    val pointer: RevocationLockPointer<T>,
    val status: RevocationLockStatus = RevocationLockStatus.LOCKED
) : OwnableState, QueryableState {

    companion object {

        /**
         * Creates a revocation lock.
         *
         * @property owner The owner of the revocation lock.
         * @property state The state that requires a revocation lock.
         * @property status Specifies whether the revocation lock is locked or unlocked.
         * @return Returns a revocation lock state.
         */
        inline fun <reified T : LinearState> create(
            owner: AbstractParty,
            state: T,
            status: RevocationLockStatus = RevocationLockStatus.LOCKED
        ) = RevocationLockState(
            owner = owner,
            pointer = RevocationLockPointer.create(state),
            status = status
        )
    }

    override val participants: List<AbstractParty> get() = listOf(owner)

    /**
     * Changes ownership of the revocation lock state.
     * @throws IllegalStateException Cannot change ownership of a revocation lock state.
     */
    override fun withNewOwner(newOwner: AbstractParty): CommandAndState {
        throw IllegalStateException("Cannot change ownership of a revocation lock state.")
    }

    /**
     * Maps this state to a persistent state.
     */
    override fun generateMappedObject(schema: MappedSchema): PersistentState = when (schema) {
        is RevocationLockSchemaV1 -> RevocationLockEntity(
            owner = owner,
            linearId = pointer.linearId.id,
            externalId = pointer.linearId.externalId,
            canonicalName = pointer.type.canonicalName,
            status = status
        )
        else -> throw IllegalArgumentException("Unrecognised schema: $schema.")
    }

    /**
     * Gets a list of supported state schemas.
     */
    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(RevocationLockSchemaV1)

    /**
     * Locks revocation of the associated state.
     */
    fun lock() = copy(status = RevocationLockStatus.LOCKED)

    /**
     * Unlocks revocation of the associated state.
     */
    fun unlock() = copy(status = RevocationLockStatus.UNLOCKED)
}