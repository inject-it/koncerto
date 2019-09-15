package io.cordacity.koncerto.contract

import net.corda.core.contracts.LinearState
import net.corda.core.contracts.StateAndRef
import net.corda.core.identity.AbstractParty

/**
 * Represents the base class for implementing attestation states.
 *
 * @property pointer A pointer to the state being attested.
 * @property attestor The participant attesting to the attested state.
 * @property attestees A set of participants for whom the state is being attested.
 * @property status Specifies whether the attestation is accepted or rejected.
 * @property metadata Allows additional information to be added to the attestation for reference.
 * @property participants The participants of the attestation state, namely the attestor, attestees and network operator.
 */
abstract class AttestationState<T : LinearState> : NetworkState() {

    abstract val pointer: AttestationPointer<T>
    abstract val attestor: AbstractParty
    abstract val attestees: Set<AbstractParty>
    abstract val status: AttestationStatus
    abstract val metadata: Map<String, String>
    override val participants: List<AbstractParty>
        get() = (attestees + attestor + network.operator).filterNotNull()

    /**
     * Creates an accepted attestation.
     *
     * @param stateAndRef The [StateAndRef] being attested, or the current state if the [StateAndRef] is null.
     * @param metadata Allows additional information to be added to the attestation for reference.
     * @return Returns an accepted attestation.
     */
    abstract fun accept(
        stateAndRef: StateAndRef<T>? = null,
        metadata: Map<String, String> = emptyMap()
    ): AttestationState<T>

    /**
     * Creates an rejected attestation.
     *
     * @param stateAndRef The [StateAndRef] being attested, or the current state if the [StateAndRef] is null.
     * @param metadata Allows additional information to be added to the attestation for reference.
     * @return Returns an rejected attestation.
     */
    abstract fun reject(
        stateAndRef: StateAndRef<T>? = null,
        metadata: Map<String, String> = emptyMap()
    ): AttestationState<T>
}