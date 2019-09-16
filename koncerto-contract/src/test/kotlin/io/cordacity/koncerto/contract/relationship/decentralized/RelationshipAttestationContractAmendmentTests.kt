package io.cordacity.koncerto.contract.relationship.decentralized

import io.cordacity.koncerto.contract.*
import io.cordacity.koncerto.contract.relationship.RelationshipAttestationContract
import io.cordacity.koncerto.contract.relationship.RelationshipContract
import net.corda.core.contracts.UniqueIdentifier
import net.corda.testing.node.ledger
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Decentralized relationship attestation contract amendment tests")
class RelationshipAttestationContractAmendmentTests : ContractTest() {

    @Test
    fun `On relationship attestation amendment, the transaction must include the Amend command`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(DECENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                input(RelationshipAttestationContract.ID, attestation)
                output(RelationshipAttestationContract.ID, attestation)
                reference(relationship.ref)
                fails()
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C), RelationshipAttestationContract.Amend)
                verifies()
            }
        }
    }

    @Test
    fun `On relationship attestation amendment, only one state must be consumed`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(DECENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                output(RelationshipAttestationContract.ID, attestation)
                reference(relationship.ref)
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C), RelationshipAttestationContract.Amend)
                failsWith(RelationshipAttestationContract.Amend.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On relationship attestation amendment, only one state must be created`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(DECENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                input(RelationshipAttestationContract.ID, attestation)
                output(RelationshipAttestationContract.ID, attestation)
                output(RelationshipAttestationContract.ID, attestation)
                reference(relationship.ref)
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C), RelationshipAttestationContract.Amend)
                failsWith(RelationshipAttestationContract.Amend.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On relationship attestation amendment, only one relationship state must be referenced`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(DECENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                input(RelationshipAttestationContract.ID, attestation)
                output(RelationshipAttestationContract.ID, attestation)
                reference(RelationshipContract.ID, relationship.state.data)
                reference(relationship.ref)
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C), RelationshipAttestationContract.Amend)
                failsWith(RelationshipAttestationContract.Amend.CONTRACT_RULE_REFERENCES)
            }
        }
    }

    @Test
    fun `On relationship attestation amendment, the attestation pointer must point to the referenced relationship state`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(DECENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                val oldPointer = attestation.pointer
                val newPointer = AttestationPointer(UniqueIdentifier(), oldPointer.stateRef, oldPointer.type)
                input(RelationshipAttestationContract.ID, attestation)
                output(RelationshipAttestationContract.ID, attestation.copy(pointer = newPointer))
                reference(relationship.ref)
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C), RelationshipAttestationContract.Amend)
                failsWith(RelationshipAttestationContract.Amend.CONTRACT_RULE_POINTER)
            }
        }
    }

    @Test
    fun `On relationship attestation amendment, all relationship participants must be included`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(DECENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                input(RelationshipAttestationContract.ID, attestation)
                output(RelationshipAttestationContract.ID, attestation.copy(attestees = setOf(IDENTITY_B.party)))
                reference(relationship.ref)
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C), RelationshipAttestationContract.Amend)
                failsWith(RelationshipAttestationContract.Amend.CONTRACT_RULE_PARTICIPANTS)
            }
        }
    }

    @Test
    fun `On relationship attestation amendment, the attestation and relationship network hash must be equal`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(DECENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                input(RelationshipAttestationContract.ID, attestation)
                output(RelationshipAttestationContract.ID, attestation.copy(network = INVALID_NETWORK))
                reference(relationship.ref)
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C), RelationshipAttestationContract.Amend)
                failsWith(RelationshipAttestationContract.Amend.CONTRACT_RULE_NETWORK_HASH)
            }
        }
    }

    @Test
    fun `On relationship attestation amendment, all participants must sign the transaction (IDENTITY_A must sign)`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(DECENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                input(RelationshipAttestationContract.ID, attestation)
                output(RelationshipAttestationContract.ID, attestation)
                reference(relationship.ref)
                command(keysOf(IDENTITY_B, IDENTITY_C), RelationshipAttestationContract.Amend)
                failsWith(RelationshipAttestationContract.Amend.CONTRACT_RULE_SIGNERS)
            }
        }
    }

    @Test
    fun `On relationship attestation amendment, all participants must sign the transaction (IDENTITY_B must sign)`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(DECENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                input(RelationshipAttestationContract.ID, attestation)
                output(RelationshipAttestationContract.ID, attestation)
                reference(relationship.ref)
                command(keysOf(IDENTITY_A, IDENTITY_C), RelationshipAttestationContract.Amend)
                failsWith(RelationshipAttestationContract.Amend.CONTRACT_RULE_SIGNERS)
            }
        }
    }

    @Test
    fun `On relationship attestation amendment, all participants must sign the transaction (IDENTITY_C must sign)`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(DECENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                input(RelationshipAttestationContract.ID, attestation)
                output(RelationshipAttestationContract.ID, attestation)
                reference(relationship.ref)
                command(keysOf(IDENTITY_A, IDENTITY_B), RelationshipAttestationContract.Amend)
                failsWith(RelationshipAttestationContract.Amend.CONTRACT_RULE_SIGNERS)
            }
        }
    }
}