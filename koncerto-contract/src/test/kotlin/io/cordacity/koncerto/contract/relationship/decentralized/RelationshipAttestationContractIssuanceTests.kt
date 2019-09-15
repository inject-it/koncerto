package io.cordacity.koncerto.contract.relationship.decentralized

import io.cordacity.koncerto.contract.*
import io.cordacity.koncerto.contract.relationship.RelationshipAttestationContract
import io.cordacity.koncerto.contract.relationship.RelationshipContract
import net.corda.core.contracts.UniqueIdentifier
import net.corda.testing.node.ledger
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Decentralized relationship attestation contract issuance tests")
class RelationshipAttestationContractIssuanceTests : ContractTest() {

    @Test
    fun `On relationship attestation issuance, the transaction must include the Issue command`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(this@ledger, DECENTRALIZED_RELATIONSHIP)
                output(RelationshipAttestationContract.ID, attestation)
                reference(relationship.ref)
                fails()
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C), RelationshipAttestationContract.Issue)
                verifies()
            }
        }
    }

    @Test
    fun `On relationship attestation issuance, zero states must be consumed`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(this@ledger, DECENTRALIZED_RELATIONSHIP)
                input(RelationshipAttestationContract.ID, attestation)
                output(RelationshipAttestationContract.ID, attestation)
                reference(relationship.ref)
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C), RelationshipAttestationContract.Issue)
                failsWith(RelationshipAttestationContract.Issue.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On relationship attestation issuance, only one state must be created`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(this@ledger, DECENTRALIZED_RELATIONSHIP)
                output(RelationshipAttestationContract.ID, attestation)
                output(RelationshipAttestationContract.ID, attestation)
                reference(relationship.ref)
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C), RelationshipAttestationContract.Issue)
                failsWith(RelationshipAttestationContract.Issue.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On relationship attestation issuance, only one relationship state must be referenced`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(this@ledger, DECENTRALIZED_RELATIONSHIP)
                output(RelationshipAttestationContract.ID, attestation)
                reference(RelationshipContract.ID, relationship.state.data)
                reference(relationship.ref)
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C), RelationshipAttestationContract.Issue)
                failsWith(RelationshipAttestationContract.Issue.CONTRACT_RULE_REFERENCES)
            }
        }
    }

    @Test
    fun `On relationship attestation issuance, the attestation pointer must point to the referenced relationship state`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(this@ledger, DECENTRALIZED_RELATIONSHIP)
                val oldPointer = attestation.pointer
                val newPointer = AttestationPointer(UniqueIdentifier(), oldPointer.stateRef, oldPointer.type)
                output(RelationshipAttestationContract.ID, attestation.copy(pointer = newPointer))
                reference(relationship.ref)
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C), RelationshipAttestationContract.Issue)
                failsWith(RelationshipAttestationContract.Issue.CONTRACT_RULE_POINTER)
            }
        }
    }

    @Test
    fun `On relationship attestation issuance, all relationship participants must be included`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(this@ledger, DECENTRALIZED_RELATIONSHIP)
                output(RelationshipAttestationContract.ID, attestation.copy(attestees = setOf(IDENTITY_B.party)))
                reference(relationship.ref)
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C), RelationshipAttestationContract.Issue)
                failsWith(RelationshipAttestationContract.Issue.CONTRACT_RULE_PARTICIPANTS)
            }
        }
    }

    @Test
    fun `On relationship attestation issuance, the attestation and relationship network hash must be equal`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(this@ledger, DECENTRALIZED_RELATIONSHIP)
                output(RelationshipAttestationContract.ID, attestation.copy(network = INVALID_NETWORK))
                reference(relationship.ref)
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C), RelationshipAttestationContract.Issue)
                failsWith(RelationshipAttestationContract.Issue.CONTRACT_RULE_NETWORK_HASH)
            }
        }
    }

    @Test
    fun `On relationship attestation issuance, all participants must sign the transaction (IDENTITY_A must sign)`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(this@ledger, DECENTRALIZED_RELATIONSHIP)
                output(RelationshipAttestationContract.ID, attestation)
                reference(relationship.ref)
                command(keysOf(IDENTITY_B, IDENTITY_C), RelationshipAttestationContract.Issue)
                failsWith(RelationshipAttestationContract.Issue.CONTRACT_RULE_SIGNERS)
            }
        }
    }

    @Test
    fun `On relationship attestation issuance, all participants must sign the transaction (IDENTITY_B must sign)`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(this@ledger, DECENTRALIZED_RELATIONSHIP)
                output(RelationshipAttestationContract.ID, attestation)
                reference(relationship.ref)
                command(keysOf(IDENTITY_A, IDENTITY_C), RelationshipAttestationContract.Issue)
                failsWith(RelationshipAttestationContract.Issue.CONTRACT_RULE_SIGNERS)
            }
        }
    }

    @Test
    fun `On relationship attestation issuance, all participants must sign the transaction (IDENTITY_C must sign)`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(this@ledger, DECENTRALIZED_RELATIONSHIP)
                output(RelationshipAttestationContract.ID, attestation)
                reference(relationship.ref)
                command(keysOf(IDENTITY_A, IDENTITY_B), RelationshipAttestationContract.Issue)
                failsWith(RelationshipAttestationContract.Issue.CONTRACT_RULE_SIGNERS)
            }
        }
    }
}