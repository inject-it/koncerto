package io.cordacity.koncerto.contract.relationship

import io.cordacity.koncerto.contract.VerifiedCommand
import io.cordacity.koncerto.contract.contractClassName
import io.cordacity.koncerto.contract.verifySingleCommand
import net.corda.core.contracts.Contract
import net.corda.core.contracts.ContractClassName
import net.corda.core.contracts.requireThat
import net.corda.core.identity.AbstractParty
import net.corda.core.transactions.LedgerTransaction
import java.security.PublicKey

class RelationshipAttestationContract : Contract {

    companion object {
        @JvmStatic
        val ID: ContractClassName = this::class.contractClassName
    }

    override fun verify(tx: LedgerTransaction) = verifySingleCommand<RelationshipAttestationContractCommand>(tx)

    interface RelationshipAttestationContractCommand : VerifiedCommand

    object Issue : RelationshipAttestationContractCommand {

        internal const val CONTRACT_RULE_INPUTS =
            "On relationship attestation issuance, zero states must be consumed."

        internal const val CONTRACT_RULE_OUTPUTS =
            "On relationship attestation issuance, only one state must be created."

        internal const val CONTRACT_RULE_REFERENCES =
            "On relationship attestation issuance, only one relationship state must be referenced."

        internal const val CONTRACT_RULE_POINTER =
            "On relationship attestation issuance, the attestation pointer must point to the referenced relationship state."

        internal const val CONTRACT_RULE_PARTICIPANTS =
            "On relationship attestation issuance, all relationship participants must be included."

        internal const val CONTRACT_RULE_NETWORK_HASH =
            "On relationship attestation issuance, the attestation and relationship network hash must be equal."

        internal const val CONTRACT_RULE_SIGNERS =
            "On relationship attestation issuance, only the attestor must sign the transaction."

        override fun verify(tx: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
            CONTRACT_RULE_INPUTS using (tx.inputs.isEmpty())
            CONTRACT_RULE_OUTPUTS using (tx.outputs.size == 1)
            CONTRACT_RULE_REFERENCES using (tx.references.size == 1)

            val referencedRelationshipState = tx.referenceInputRefsOfType<RelationshipState<*>>().single()
            val attestationOutputState = tx.outputsOfType<RelationshipAttestationState>().single()
            val missingParticipants =
                referencedRelationshipState.state.data.participants - attestationOutputState.participants

            CONTRACT_RULE_POINTER using (attestationOutputState.pointer.isPointingTo(referencedRelationshipState))
            CONTRACT_RULE_PARTICIPANTS using (missingParticipants == emptyList<AbstractParty>())
            CONTRACT_RULE_NETWORK_HASH using (attestationOutputState.network.hash == referencedRelationshipState.state.data.network.hash)
            CONTRACT_RULE_SIGNERS using (attestationOutputState.attestor.owningKey == signers.single())
        }
    }

    object Amend : RelationshipAttestationContractCommand {

        internal const val CONTRACT_RULE_INPUTS =
            "On relationship attestation amendment, only one state must be consumed."

        internal const val CONTRACT_RULE_OUTPUTS =
            "On relationship attestation amendment, only one state must be created."

        internal const val CONTRACT_RULE_REFERENCES =
            "On relationship attestation amendment, only one relationship state must be referenced."

        internal const val CONTRACT_RULE_POINTER =
            "On relationship attestation amendment, the attestation pointer must point to the referenced relationship state."

        internal const val CONTRACT_RULE_PARTICIPANTS =
            "On relationship attestation amendment, all relationship participants must be included."

        internal const val CONTRACT_RULE_NETWORK_HASH =
            "On relationship attestation amendment, the attestation and relationship network hash must be equal."

        internal const val CONTRACT_RULE_SIGNERS =
            "On relationship attestation amendment, only the attestor must sign the transaction."

        override fun verify(tx: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
            CONTRACT_RULE_INPUTS using (tx.inputs.size == 1)
            CONTRACT_RULE_OUTPUTS using (tx.outputs.size == 1)
            CONTRACT_RULE_REFERENCES using (tx.references.size == 1)

            val referencedRelationshipState = tx.referenceInputRefsOfType<RelationshipState<*>>().single()
            val attestationOutputState = tx.outputsOfType<RelationshipAttestationState>().single()
            val missingParticipants =
                referencedRelationshipState.state.data.participants - attestationOutputState.participants

            CONTRACT_RULE_POINTER using (attestationOutputState.pointer.isPointingTo(referencedRelationshipState))
            CONTRACT_RULE_PARTICIPANTS using (missingParticipants == emptyList<AbstractParty>())
            CONTRACT_RULE_NETWORK_HASH using (attestationOutputState.network.hash == referencedRelationshipState.state.data.network.hash)
            CONTRACT_RULE_SIGNERS using (attestationOutputState.attestor.owningKey == signers.single())
        }
    }

    object Revoke : RelationshipAttestationContractCommand {

        internal const val CONTRACT_RULE_INPUTS =
            "On relationship attestation revocation, only one state must be consumed."

        internal const val CONTRACT_RULE_OUTPUTS =
            "On relationship attestation revocation, zero states must be created."

        internal const val CONTRACT_RULE_SIGNERS =
            "On relationship attestation revocation, only the attestor must sign the transaction."

        override fun verify(tx: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
            CONTRACT_RULE_INPUTS using (tx.inputs.size == 1)
            CONTRACT_RULE_OUTPUTS using (tx.outputs.isEmpty())

            val attestation = tx.inputsOfType<RelationshipAttestationState>().single()

            CONTRACT_RULE_SIGNERS using (attestation.attestor.owningKey == signers.single())
        }
    }
}