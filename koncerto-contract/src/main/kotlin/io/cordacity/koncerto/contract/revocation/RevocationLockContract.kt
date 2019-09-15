package io.cordacity.koncerto.contract.revocation

import io.cordacity.koncerto.contract.VerifiedCommand
import io.cordacity.koncerto.contract.contractClassName
import io.cordacity.koncerto.contract.verifySingleCommand
import net.corda.core.contracts.Contract
import net.corda.core.contracts.ContractClassName
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction
import java.security.PublicKey

class RevocationLockContract : Contract {

    companion object {
        @JvmStatic
        val ID: ContractClassName = this::class.contractClassName
    }

    override fun verify(tx: LedgerTransaction) = verifySingleCommand<RevocationLockContractCommand>(tx)

    interface RevocationLockContractCommand : VerifiedCommand

    object Create : RevocationLockContractCommand {

        internal const val CONTRACT_RULE_OUTPUTS =
            "On revocation lock creation, at least one revocation lock state must be created."

        internal const val CONTRACT_RULE_SIGNERS =
            "On revocation lock creation, the owner of the revocation lock state must sign the transaction."

        override fun verify(tx: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
            val revocationLockOutputs = tx.outputsOfType<RevocationLockState<*>>()
            CONTRACT_RULE_OUTPUTS using (revocationLockOutputs.isNotEmpty())
            CONTRACT_RULE_SIGNERS using (revocationLockOutputs.all { it.owner.owningKey in signers })
        }
    }

    object Update : RevocationLockContractCommand {

        internal const val CONTRACT_RULE_INPUTS =
            "On revocation lock updating, only one state must be consumed."

        internal const val CONTRACT_RULE_OUTPUTS =
            "On revocation lock updating, only one state must be created."

        internal const val CONTRACT_RULE_STATUS =
            "On revocation lock updating, the status must change."

        internal const val CONTRACT_RULE_OWNER =
            "On revocation lock updating, the owner must not change."

        internal const val CONTRACT_RULE_SIGNERS =
            "On revocation lock updating, only the owner must sign the transaction."

        override fun verify(tx: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
            CONTRACT_RULE_INPUTS using (tx.inputs.size == 1)
            CONTRACT_RULE_OUTPUTS using (tx.outputs.size == 1)

            val revocationLockInputState = tx.inputsOfType<RevocationLockState<*>>().single()
            val revocationLockOutputState = tx.outputsOfType<RevocationLockState<*>>().single()

            CONTRACT_RULE_STATUS using (revocationLockInputState.status != revocationLockOutputState.status)
            CONTRACT_RULE_OWNER using (revocationLockInputState.owner == revocationLockOutputState.owner)
            CONTRACT_RULE_SIGNERS using (revocationLockOutputState.owner.owningKey in signers)
        }
    }

    object Delete : RevocationLockContractCommand {

        internal const val CONTRACT_RULE_INPUTS =
            "On revocation lock deletion, only one state must be consumed."

        internal const val CONTRACT_RULE_OUTPUTS =
            "On revocation lock deletion, zero states must be created."

        internal const val CONTRACT_RULE_SIGNERS =
            "On revocation lock deletion, only the owner must sign the transaction."

        override fun verify(tx: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
            CONTRACT_RULE_INPUTS using (tx.inputs.size == 1)
            CONTRACT_RULE_OUTPUTS using (tx.outputs.isEmpty())

            val revocationLockInputState = tx.inputsOfType<RevocationLockState<*>>().single()

            CONTRACT_RULE_SIGNERS using (revocationLockInputState.owner.owningKey in signers)
        }
    }
}