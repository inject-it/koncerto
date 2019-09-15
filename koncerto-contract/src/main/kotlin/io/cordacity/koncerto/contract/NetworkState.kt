package io.cordacity.koncerto.contract

import net.corda.core.contracts.ContractState
import net.corda.core.contracts.LinearState
import net.corda.core.schemas.QueryableState
import net.corda.core.serialization.CordaSerializable

/**
 * Represents the base class for implementing network states.
 *
 * @property network The identity of the network.
 */
@CordaSerializable
abstract class NetworkState : ContractState, LinearState, QueryableState {
    abstract val network: Network
}