package io.cordacity.koncerto.contract

import net.corda.core.serialization.CordaSerializable

/**
 * Specifies the status of the attestation.
 */
@CordaSerializable
enum class AttestationStatus {

    /**
     * The attestation was accepted.
     */
    ACCEPTED,

    /**
     * The attestation was rejected.
     */
    REJECTED
}