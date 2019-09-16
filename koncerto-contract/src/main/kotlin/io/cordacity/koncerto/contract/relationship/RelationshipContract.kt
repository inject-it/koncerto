package io.cordacity.koncerto.contract.relationship

import io.cordacity.koncerto.contract.VerifiedCommand
import io.cordacity.koncerto.contract.contractClassName
import io.cordacity.koncerto.contract.revocation.RevocationLockState
import io.cordacity.koncerto.contract.verifySingleCommand
import net.corda.core.contracts.Contract
import net.corda.core.contracts.ContractClassName
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction
import java.security.PublicKey

class RelationshipContract : Contract {

    companion object {
        @JvmStatic
        val ID: ContractClassName = this::class.contractClassName
    }

    override fun verify(tx: LedgerTransaction) = verifySingleCommand<RelationshipContractCommand>(tx)

    interface RelationshipContractCommand : VerifiedCommand

    object Issue : RelationshipContractCommand {

        internal const val CONTRACT_RULE_INPUTS =
            "On relationship issuance, zero states must be consumed."

        internal const val CONTRACT_RULE_OUTPUTS =
            "On relationship issuance, only one relationship state must be created."

        internal const val CONTRACT_RULE_LOCKS =
            "On relationship issuance, revocation locks must be issued for all participants."

        internal const val CONTRACT_REVOCATION_LOCK_POINTERS =
            "On relationship issuance, all revocation locks must point to the relationship state."

        internal const val CONTRACT_RULE_PREVIOUS_REF =
            "On relationship issuance, the previous state reference must be null."

        internal const val CONTRACT_RULE_SIGNERS =
            "On relationship issuance, all participants must sign the transaction."

        override fun verify(tx: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
            CONTRACT_RULE_INPUTS using (tx.inputs.isEmpty())
            CONTRACT_RULE_OUTPUTS using (tx.outputsOfType<RelationshipState<*>>().size == 1)

            val relationshipOutputState = tx.outputsOfType<RelationshipState<*>>().single()
            val revocationLockOutputStates = tx.outputsOfType<RevocationLockState<*>>()

            val validLockCount = relationshipOutputState.participants.size == revocationLockOutputStates.size
            val locksForAllParticipants = relationshipOutputState.participants
                .containsAll(revocationLockOutputStates.map { it.owner })

            CONTRACT_RULE_LOCKS using (validLockCount && locksForAllParticipants)
            CONTRACT_REVOCATION_LOCK_POINTERS using (revocationLockOutputStates.all {
                it.pointer.linearId == relationshipOutputState.linearId
            })
            CONTRACT_RULE_PREVIOUS_REF using (relationshipOutputState.previousStateRef == null)
            CONTRACT_RULE_SIGNERS using (relationshipOutputState.participants.all { it.owningKey in signers })
        }
    }

    object Amend : RelationshipContractCommand {

        internal const val CONTRACT_RULE_INPUTS =
            "On relationship amendment, only one state must be consumed."

        internal const val CONTRACT_RULE_OUTPUTS =
            "On relationship amendment, only one state must be created."

        internal const val CONTRACT_RULE_NETWORK_HASH =
            "On relationship amendment, the network hash must not change."

        internal const val CONTRACT_RULE_PREVIOUS_REF =
            "On relationship amendment, the previous state reference must be equal to the input state reference."

        internal const val CONTRACT_RULE_SIGNERS =
            "On relationship amendment, all participants must sign the transaction."

        override fun verify(tx: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
            CONTRACT_RULE_INPUTS using (tx.inputs.size == 1)
            CONTRACT_RULE_OUTPUTS using (tx.outputs.size == 1)

            val relationshipInputStateAndRef = tx.inRefsOfType<RelationshipState<*>>().single()
            val relationshipInputState = relationshipInputStateAndRef.state.data
            val relationshipOutputState = tx.outputsOfType<RelationshipState<*>>().single()

            CONTRACT_RULE_NETWORK_HASH using (relationshipInputState.network == relationshipOutputState.network)
            CONTRACT_RULE_PREVIOUS_REF using (relationshipOutputState.previousStateRef == relationshipInputStateAndRef.ref)
            CONTRACT_RULE_SIGNERS using (relationshipOutputState.participants.all { it.owningKey in signers })
        }
    }

    object Revoke : RelationshipContractCommand {

        internal const val CONTRACT_RULE_INPUTS =
            "On relationship revocation, only one state must be consumed."

        internal const val CONTRACT_RULE_OUTPUTS =
            "On relationship revocation, zero states must be created."

        internal const val CONTRACT_RULE_SIGNERS =
            "On relationship revocation, all participants must sign the transaction."

        override fun verify(tx: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
            CONTRACT_RULE_INPUTS using (tx.inputs.size == 1)
            CONTRACT_RULE_OUTPUTS using (tx.outputs.isEmpty())

            val relationshipInputState = tx.inputsOfType<RelationshipState<*>>().single()

            CONTRACT_RULE_SIGNERS using (relationshipInputState.participants.all { it.owningKey in signers })
        }
    }
}
