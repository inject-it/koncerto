package io.cordacity.koncerto.contract

import net.corda.core.crypto.SecureHash

/**
 * Provides functionality for objects that require a hash property.
 *
 * @property hash  A SHA-256 hashed representation of the object.
 */
interface Hashable {
    val hash: SecureHash
}