package io.cordacity.koncerto.contract

import net.corda.core.contracts.LinearState
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.StateRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.QueryCriteria.VaultQueryCriteria
import net.corda.core.serialization.CordaSerializable
import java.util.*

/**
 * Represents a pointer to an attested state.
 *
 * @property linearId The unique identifier of the attested state.
 * @property stateRef The state reference of the attested state.
 * @property type The type of the attested state.
 */
@CordaSerializable
class AttestationPointer<T : LinearState>(
    val linearId: UniqueIdentifier,
    val stateRef: StateRef,
    val type: Class<T>
) : Resolvable<T> {

    companion object {

        /**
         * Creates an [AttestationPointer] instance from the specified [StateAndRef] instance.
         *
         * @param stateAndRef The [StateAndRef] instance which the attestation pointer will point to.
         * @return Returns an [AttestationPointer] pointing to the specified [StateAndRef] instance.
         */
        inline fun <reified T : LinearState> create(stateAndRef: StateAndRef<T>): AttestationPointer<T> {
            return AttestationPointer(stateAndRef.state.data.linearId, stateAndRef.ref, T::class.java)
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
            ?: throw IllegalStateException("Failed to obtain state with id '$linearId' and ref '$stateRef'.")
    }

    /**
     * Resolves a [StateAndRef] using a [ServiceHub] instance.
     *
     * @param serviceHub The [ServiceHub] instance to use to resolve the [StateAndRef].
     * @return Returns a resolved [StateAndRef].
     */
    override fun resolve(serviceHub: ServiceHub): StateAndRef<T> {
        return serviceHub.toStateAndRef(stateRef)
    }

    /**
     * Gets a [QueryCriteria] representing the state resolution query.
     * @return Returns a [QueryCriteria] representing the state resolution query.
     */
    override fun toQueryCriteria(): QueryCriteria {
        return VaultQueryCriteria(
            contractStateTypes = setOf(type),
            stateRefs = listOf(stateRef),
            status = Vault.StateStatus.ALL
        )
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
                && other.stateRef == stateRef
                && other.type == type)
    }

    /**
     * Serves as the default hash code implementation.
     * @return Returns a unique hash code for this object instance.
     */
    override fun hashCode() = Objects.hash(linearId, stateRef, type)

    /**
     * Gets a string representation of this object instance.
     * @return Returns a string representation of this object instance.
     */
    override fun toString() = "AttestationPointer: linearId = $linearId, stateRef = $stateRef, type = $type."

    /**
     * Determines whether this attestation pointer is pointing to the specified [StateAndRef].
     *
     * @param stateAndRef The [StateAndRef] to check against this attestation pointer.
     * @return Returns true if this attestation pointer is pointing to the specified [StateAndRef]; otherwise, false.
     */
    fun isPointingTo(stateAndRef: StateAndRef<T>): Boolean {
        return linearId == stateAndRef.state.data.linearId && stateRef == stateAndRef.ref
    }
}