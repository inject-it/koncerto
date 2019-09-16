package io.cordacity.koncerto.contract

import io.cordacity.koncerto.contract.HashUtils.createParticipantsHash
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.AbstractParty
import net.corda.core.serialization.CordaSerializable
import java.util.*

/**
 * Represents the base class for implementing relationship configuration.
 *
 * @property name The name of the configuration.
 * @property networkIdentities The Corda network identities of the participants.
 * @property normalizedName The normalize name of the configuration.
 * @property hash A SHA-256 hashed representation of the configuration.
 */
@CordaSerializable
abstract class Configuration : Hashable {
    abstract val name: String
    abstract val networkIdentities: Set<AbstractParty>

    val normalizedName: String
        get() = name.toLowerCase()

    override val hash: SecureHash
        get() = SecureHash.sha256("$normalizedName${createParticipantsHash(networkIdentities)}")

    /**
     * Compares this object for equality with the specified object.
     *
     * @param other The object to compare with this object.
     * @return Returns true if the objects are considered equal; otherwise, false.
     */
    override fun equals(other: Any?): Boolean {
        return other === this || (other != null
                && other is Configuration
                && other.hash == hash
                && other.normalizedName == normalizedName)
    }

    /**
     * Serves as the default hash code implementation.
     * @return Returns a unique hash code for this object instance.
     */
    override fun hashCode() = Objects.hash(hash)

    /**
     * Gets a string representation of this object instance.
     * @return Returns a string representation of this object instance.
     */
    override fun toString() = "Role: name = $name, normalized name = $normalizedName"
}