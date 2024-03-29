package io.cordacity.koncerto.contract

import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import java.util.*

/**
 * Represents the base class for implementing identity.
 *
 * @property networkIdentity The Corda network identity of the participant.
 */
@CordaSerializable
abstract class Identity {
    abstract val networkIdentity: Party

    /**
     * Compares this object for equality with the specified object.
     *
     * @param other The object to compare with this object.
     * @return Returns true if the objects are considered equal; otherwise, false.
     */
    override fun equals(other: Any?): Boolean {
        return other === this || (other != null
                && other is Identity
                && other.networkIdentity == networkIdentity)
    }

    /**
     * Serves as the default hash code implementation.
     * @return Returns a unique hash code for this object instance.
     */
    override fun hashCode() = Objects.hash(networkIdentity)

    /**
     * Gets a string representation of this object instance.
     * @return Returns a string representation of this object instance.
     */
    override fun toString() = "Identity: ${networkIdentity.name}"
}