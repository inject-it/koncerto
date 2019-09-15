package io.cordacity.koncerto.contract

import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StateAndRef
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.vault.QueryCriteria

/**
 * Provides functionality to resolve states from a [CordaRPCOps] or [ServiceHub] instance.
 */
interface Resolvable<T : ContractState> {

    /**
     * Resolves a [StateAndRef] using a [CordaRPCOps] instance.
     *
     * @param cordaRPCOps The [CordaRPCOps] instance to use to resolve the [StateAndRef].
     * @return Returns a resolved [StateAndRef].
     */
    fun resolve(cordaRPCOps: CordaRPCOps): StateAndRef<T>

    /**
     * Resolves a [StateAndRef] using a [ServiceHub] instance.
     *
     * @param serviceHub The [ServiceHub] instance to use to resolve the [StateAndRef].
     * @return Returns a resolved [StateAndRef].
     */
    fun resolve(serviceHub: ServiceHub): StateAndRef<T>

    /**
     * Gets a [QueryCriteria] representing the state resolution query.
     * @return Returns a [QueryCriteria] representing the state resolution query.
     */
    fun toQueryCriteria(): QueryCriteria
}