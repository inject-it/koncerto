package io.cordacity.koncerto.contract

import net.corda.core.crypto.SecureHash
import net.corda.core.identity.AbstractParty
import net.corda.core.serialization.CordaSerializable
import java.util.*

/**
 * Represents the identity of a network.
 *
 * @property name The name of the network.
 * @property operator The identity of the network operator, or null if the network is decentralized.
 * @property normalizedName The normalized name of the network.
 * @property hash A SHA-256 hashed representation of the identity of a network.
 */
@CordaSerializable
class Network(val name: String, val operator: AbstractParty? = null) {

    val normalizedName: String get() = name.toLowerCase()

    val hash: SecureHash get() = SecureHash.sha256("$normalizedName$operator")

    /**
     * Compares this object for equality with the specified object.
     *
     * @param other The object to compare with this object.
     * @return Returns true if the objects are considered equal; otherwise, false.
     */
    override fun equals(other: Any?): Boolean {
        return other === this || (other != null
                && other is Network
                && other.operator == operator
                && other.normalizedName == normalizedName)
    }

    /**
     * Serves as the default hash code implementation.
     * @return Returns a unique hash code for this object instance.
     */
    override fun hashCode() = Objects.hash(normalizedName, operator)

    /**
     * Gets a string representation of this object instance.
     * @return Returns a string representation of this object instance.
     */
    override fun toString() = "Network: name = $name, normalized name = $normalizedName, operator = $operator"
}