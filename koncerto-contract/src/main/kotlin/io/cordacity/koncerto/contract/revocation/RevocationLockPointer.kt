package io.cordacity.koncerto.contract.revocation

import io.cordacity.koncerto.contract.AttestationPointer
import io.cordacity.koncerto.contract.Resolvable
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.QueryCriteria.LinearStateQueryCriteria
import net.corda.core.serialization.CordaSerializable
import java.util.*

/**
 * Represents a pointer to a state that requires revocation locking.
 *
 * @property linearId The unique identifier of the revocation locked state.
 * @property type The type of the revocation locked state.
 */
@CordaSerializable
class RevocationLockPointer<T : LinearState>(
    val linearId: UniqueIdentifier,
    val type: Class<T>
) : Resolvable<T> {

    companion object {

        /**
         * Creates an [RevocationLockPointer] instance from the specified [LinearState] instance.
         *
         * @param linearState The [StateAndRef] instance which the attestation pointer will point to.
         * @return Returns an [RevocationLockPointer] pointing to the specified [LinearState] instance.
         */
        inline fun <reified T : LinearState> create(linearState: T): RevocationLockPointer<T> {
            return RevocationLockPointer(linearState.linearId, T::class.java)
        }
    }

    /**
     * Resolves a [StateAndRef] using a [CordaRPCOps] instance.
     *
     * @param cordaRPCOps The [CordaRPCOps] instance to use to resolve the [StateAndRef].
     * @return Returns a resolved [StateAndRef].
     */
    override fun resolve(cordaRPCOps: CordaRPCOps): StateAndRef<T> {
        return cordaRPCOps.vaultQueryByCriteria(toQueryCriteria(), type).states.singleOrNull()
            ?: throw IllegalStateException("Failed to obtain state with id '$linearId'.")
    }

    /**
     * Resolves a [StateAndRef] using a [ServiceHub] instance.
     *
     * @param serviceHub The [ServiceHub] instance to use to resolve the [StateAndRef].
     * @return Returns a resolved [StateAndRef].
     */
    override fun resolve(serviceHub: ServiceHub): StateAndRef<T> {
        return serviceHub.vaultService.queryBy(type, toQueryCriteria()).states.singleOrNull()
            ?: throw IllegalStateException("Failed to obtain state with id '$linearId'.")
    }

    /**
     * Gets a [QueryCriteria] representing the state resolution query.
     * @return Returns a [QueryCriteria] representing the state resolution query.
     */
    override fun toQueryCriteria(): QueryCriteria {
        return LinearStateQueryCriteria(null, listOf(linearId), Vault.StateStatus.UNCONSUMED, setOf(type))
    }

    /**
     * Compares this object for equality with the specified object.
     *
     * @param other The object to compare with this object.
     * @return Returns true if the objects are considered equal; otherwise, false.
     */
    override fun equals(other: Any?): Boolean {
        return other === this || (other != null
                && other is AttestationPointer<*>
                && other.linearId == linearId
                && other.type == type)
    }

    /**
     * Serves as the default hash code implementation.
     * @return Returns a unique hash code for this object instance.
     */
    override fun hashCode() = Objects.hash(linearId, type)

    /**
     * Gets a string representation of this object instance.
     * @return Returns a string representation of this object instance.
     */
    override fun toString() = "RevocationLockPointer: linearId = $linearId, type = $type."

    /**
     * Determines whether this revocation lock pointer is pointing to the specified [LinearState].
     *
     * @param linearState The [LinearState] to check against this revocation lock pointer.
     * @return Returns true if this revocation lock pointer is pointing to the specified [LinearState]; otherwise, false.
     */
    fun isPointingTo(linearState: LinearState): Boolean {
        return linearId == linearState.linearId
    }
}