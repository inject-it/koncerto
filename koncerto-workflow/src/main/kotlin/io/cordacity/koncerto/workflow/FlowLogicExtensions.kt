package io.cordacity.koncerto.workflow

import io.cordacity.koncerto.contract.Network
import io.cordacity.koncerto.contract.membership.MembershipState
import io.cordacity.koncerto.workflow.membership.FindLocalMembershipAttestationFlow
import io.cordacity.koncerto.workflow.membership.FindLocalMembershipFlow
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.FlowException
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.utilities.ProgressTracker
import java.security.PublicKey

private typealias MembershipList = List<StateAndRef<MembershipState<*>>>

val FlowLogic<*>.firstNotary: Party get() = serviceHub.networkMapCache.notaryIdentities.first()

fun FlowLogic<*>.currentStep(step: ProgressTracker.Step) {
    progressTracker?.currentStep = step
    logger.info(step.label)
}

fun FlowLogic<*>.keysFor(parties: Iterable<AbstractParty>): List<PublicKey> = parties.map { it.owningKey }

fun FlowLogic<*>.flowSessionFor(party: AbstractParty): FlowSession {
    val wellKnownParty = serviceHub.identityService.wellKnownPartyFromAnonymous(party)
        ?: throw FlowException("Failed to resolve well known party from anonymous party.")
    return initiateFlow(wellKnownParty)
}

fun FlowLogic<*>.flowSessionsFor(parties: Iterable<AbstractParty>) = parties.map { flowSessionFor(it) }

fun FlowLogic<*>.checkSessionsForAllCounterparties(state: ContractState, sessions: Iterable<FlowSession>) {
    val counterparties = state.participants - ourIdentity
    if (counterparties.isNotEmpty() && sessions.any { it.counterparty !in counterparties }) {
        throw FlowException("Flow sessions are required for all counter-parties.")
    }
}

fun FlowLogic<*>.checkMembershipsAndAttestations(parties: Iterable<AbstractParty>, network: Network) {
    if (network.operator == null || network.operator == ourIdentity) {
        parties.forEach {
            val membership = subFlow<MembershipList>(FindLocalMembershipFlow(it, network)).singleOrNull()
                ?: throw FlowException("Failed to obtain membership state for counter-party: $it")

            subFlow(FindLocalMembershipAttestationFlow(membership)).singleOrNull()
                ?: throw FlowException("Failed to obtain membership attestation state for counter-party: $it")
        }
    }
}