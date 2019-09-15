package io.cordacity.koncerto.contract.revocation

import net.corda.core.serialization.CordaSerializable

/**
 * Specifies the status of the revocation lock.
 */
@CordaSerializable
enum class RevocationLockStatus {

    /**
     * The revocation lock is locked.
     */
    LOCKED,

    /**
     * The revocation lock is unlocked.
     */
    UNLOCKED
}