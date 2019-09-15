package io.cordacity.koncerto.contract.membership

import io.cordacity.koncerto.contract.VerifiedCommand
import io.cordacity.koncerto.contract.contractClassName
import io.cordacity.koncerto.contract.verifySingleCommand
import net.corda.core.contracts.Contract
import net.corda.core.contracts.ContractClassName
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction
import java.security.PublicKey

class MembershipAttestationContract : Contract {

    companion object {
        @JvmStatic
        val ID: ContractClassName = this::class.contractClassName
    }

    override fun verify(tx: LedgerTransaction) = verifySingleCommand<MembershipAttestationContractCommand>(tx)

    interface MembershipAttestationContractCommand : VerifiedCommand

    object Issue : MembershipAttestationContractCommand {

        internal const val CONTRACT_RULE_INPUTS =
            "On membership attestation issuance, zero states must be consumed."

        internal const val CONTRACT_RULE_OUTPUTS =
            "On membership attestation issuance, only one state must be created."

        internal const val CONTRACT_RULE_REFERENCES =
            "On membership attestation issuance, only one membership state must be referenced."

        internal const val CONTRACT_RULE_POINTER =
            "On membership attestation issuance, the attestation pointer must point to the referenced membership state."

        internal const val CONTRACT_RULE_MEMBERSHIP =
            "On membership attestation issuance, the attestee must be the network identity of the referenced membership state."

        internal const val CONTRACT_RULE_OPERATOR =
            "On membership attestation issuance, if present, only the network operator must attest membership."

        internal const val CONTRACT_RULE_NETWORK_HASH =
            "On membership attestation issuance, the attestation and membership network hash must be equal."

        internal const val CONTRACT_RULE_SIGNERS =
            "On membership attestation issuance, only the attestor must sign the transaction."

        override fun verify(tx: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
            CONTRACT_RULE_INPUTS using (tx.inputs.isEmpty())
            CONTRACT_RULE_OUTPUTS using (tx.outputs.size == 1)
            CONTRACT_RULE_REFERENCES using (tx.references.size == 1)

            val referencedMembershipState = tx.referenceInputRefsOfType<MembershipState<*>>().single()
            val attestationOutputState = tx.outputsOfType<MembershipAttestationState>().single()

            CONTRACT_RULE_POINTER using (attestationOutputState.pointer.isPointingTo(referencedMembershipState))
            CONTRACT_RULE_MEMBERSHIP using (attestationOutputState.attestee == referencedMembershipState.state.data.identity.networkIdentity)

            if (attestationOutputState.network.operator != null) {
                CONTRACT_RULE_OPERATOR using (attestationOutputState.network.operator == attestationOutputState.attestor)
            }

            CONTRACT_RULE_NETWORK_HASH using (attestationOutputState.network.hash == referencedMembershipState.state.data.network.hash)
            CONTRACT_RULE_SIGNERS using (attestationOutputState.attestor.owningKey == signers.single())
        }
    }

    object Amend : MembershipAttestationContractCommand {

        internal const val CONTRACT_RULE_INPUTS =
            "On membership attestation amendment, only one state must be consumed."

        internal const val CONTRACT_RULE_OUTPUTS =
            "On membership attestation amendment, only one state must be created."

        internal const val CONTRACT_RULE_REFERENCES =
            "On membership attestation amendment, only one membership state must be referenced."

        internal const val CONTRACT_RULE_POINTER =
            "On membership attestation amendment, the attestation pointer must point to the referenced membership state."

        internal const val CONTRACT_RULE_MEMBERSHIP =
            "On membership attestation amendment, the attestee must be the network identity of the referenced membership state."

        internal const val CONTRACT_RULE_OPERATOR =
            "On membership attestation amendment, if present, only the network operator must attest membership."

        internal const val CONTRACT_RULE_NETWORK_HASH =
            "On membership attestation amendment, the attestation and membership network hash must be equal."

        internal const val CONTRACT_RULE_SIGNERS =
            "On membership attestation amendment, only the attestor must sign the transaction."

        override fun verify(tx: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
            CONTRACT_RULE_INPUTS using (tx.inputs.size == 1)
            CONTRACT_RULE_OUTPUTS using (tx.outputs.size == 1)
            CONTRACT_RULE_REFERENCES using (tx.references.size == 1)

            val referencedMembershipState = tx.referenceInputRefsOfType<MembershipState<*>>().single()
            val attestationOutputState = tx.outputsOfType<MembershipAttestationState>().single()

            CONTRACT_RULE_POINTER using (attestationOutputState.pointer.isPointingTo(referencedMembershipState))
            CONTRACT_RULE_MEMBERSHIP using (attestationOutputState.attestee == referencedMembershipState.state.data.identity.networkIdentity)
            CONTRACT_RULE_NETWORK_HASH using (attestationOutputState.network.hash == referencedMembershipState.state.data.network.hash)

            if (attestationOutputState.network.operator != null) {
                CONTRACT_RULE_OPERATOR using (attestationOutputState.network.operator == attestationOutputState.attestor)
            }

            CONTRACT_RULE_SIGNERS using (attestationOutputState.attestor.owningKey == signers.single())
        }
    }

    object Revoke : MembershipAttestationContractCommand {

        internal const val CONTRACT_RULE_INPUTS =
            "On membership attestation revocation, only one state must be consumed."

        internal const val CONTRACT_RULE_OUTPUTS =
            "On membership attestation revocation, zero states must be created."

        internal const val CONTRACT_RULE_SIGNERS =
            "On membership attestation revocation, only the attestor must sign the transaction."

        override fun verify(tx: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
            CONTRACT_RULE_INPUTS using (tx.inputs.size == 1)
            CONTRACT_RULE_OUTPUTS using (tx.outputs.isEmpty())

            val attestationInputState = tx.inputsOfType<MembershipAttestationState>().single()

            CONTRACT_RULE_SIGNERS using (attestationInputState.attestor.owningKey == signers.single())
        }
    }
}