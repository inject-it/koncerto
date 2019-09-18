package io.cordacity.koncerto.contract.relationship

import io.cordacity.koncerto.contract.*
import io.cordacity.koncerto.contract.relationship.RelationshipAttestationContract
import net.corda.core.contracts.UniqueIdentifier
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class RelationshipAttestationContractIssuanceTests : ContractTest() {

    @Test
    fun `On relationship attestation issuance, the transaction must include the Issue command (centralized)`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(CENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                output(RelationshipAttestationContract.ID, attestation)
                reference(relationship.ref)
                fails()
                command(keysOf(IDENTITY_A), RelationshipAttestationContract.Issue)
                verifies()
            }
        }
    }

    @Test
    fun `On relationship attestation issuance, the transaction must include the Issue command (decentralized)`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(DECENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                output(RelationshipAttestationContract.ID, attestation)
                reference(relationship.ref)
                fails()
                command(keysOf(IDENTITY_A), RelationshipAttestationContract.Issue)
                verifies()
            }
        }
    }

    @Test
    fun `On relationship attestation issuance, zero states must be consumed (centralized)`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(CENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                input(RelationshipAttestationContract.ID, attestation)
                output(RelationshipAttestationContract.ID, attestation)
                reference(relationship.ref)
                command(keysOf(IDENTITY_A), RelationshipAttestationContract.Issue)
                failsWith(RelationshipAttestationContract.Issue.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On relationship attestation issuance, zero states must be consumed (decentralized)`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(DECENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                input(RelationshipAttestationContract.ID, attestation)
                output(RelationshipAttestationContract.ID, attestation)
                reference(relationship.ref)
                command(keysOf(IDENTITY_A), RelationshipAttestationContract.Issue)
                failsWith(RelationshipAttestationContract.Issue.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On relationship attestation issuance, only one state must be created (centralized)`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(CENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                output(RelationshipAttestationContract.ID, attestation)
                output(RelationshipAttestationContract.ID, attestation)
                reference(relationship.ref)
                command(keysOf(IDENTITY_A), RelationshipAttestationContract.Issue)
                failsWith(RelationshipAttestationContract.Issue.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On relationship attestation issuance, only one state must be created (decentralized)`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(DECENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                output(RelationshipAttestationContract.ID, attestation)
                output(RelationshipAttestationContract.ID, attestation)
                reference(relationship.ref)
                command(keysOf(IDENTITY_A), RelationshipAttestationContract.Issue)
                failsWith(RelationshipAttestationContract.Issue.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On relationship attestation issuance, only one relationship state must be referenced (centralized)`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(CENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                output(RelationshipAttestationContract.ID, attestation)
                reference(RelationshipContract.ID, relationship.state.data)
                reference(relationship.ref)
                command(keysOf(IDENTITY_A), RelationshipAttestationContract.Issue)
                failsWith(RelationshipAttestationContract.Issue.CONTRACT_RULE_REFERENCES)
            }
        }
    }

    @Test
    fun `On relationship attestation issuance, only one relationship state must be referenced (decentralized)`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(DECENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                output(RelationshipAttestationContract.ID, attestation)
                reference(RelationshipContract.ID, relationship.state.data)
                reference(relationship.ref)
                command(keysOf(IDENTITY_A), RelationshipAttestationContract.Issue)
                failsWith(RelationshipAttestationContract.Issue.CONTRACT_RULE_REFERENCES)
            }
        }
    }

    @Test
    fun `On relationship attestation issuance, the attestation pointer must point to the referenced relationship state (centralized)`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(CENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                val oldPointer = attestation.pointer
                val newPointer = AttestationPointer(UniqueIdentifier(), oldPointer.stateRef, oldPointer.type)
                output(RelationshipAttestationContract.ID, attestation.copy(pointer = newPointer))
                reference(relationship.ref)
                command(keysOf(IDENTITY_A), RelationshipAttestationContract.Issue)
                failsWith(RelationshipAttestationContract.Issue.CONTRACT_RULE_POINTER)
            }
        }
    }

    @Test
    fun `On relationship attestation issuance, the attestation pointer must point to the referenced relationship state (decentralized)`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(DECENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                val oldPointer = attestation.pointer
                val newPointer = AttestationPointer(UniqueIdentifier(), oldPointer.stateRef, oldPointer.type)
                output(RelationshipAttestationContract.ID, attestation.copy(pointer = newPointer))
                reference(relationship.ref)
                command(keysOf(IDENTITY_A), RelationshipAttestationContract.Issue)
                failsWith(RelationshipAttestationContract.Issue.CONTRACT_RULE_POINTER)
            }
        }
    }

    @Test
    fun `On relationship attestation issuance, all relationship participants must be included (centralized)`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(CENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                output(RelationshipAttestationContract.ID, attestation.copy(attestees = setOf(IDENTITY_B.party)))
                reference(relationship.ref)
                command(keysOf(IDENTITY_A), RelationshipAttestationContract.Issue)
                failsWith(RelationshipAttestationContract.Issue.CONTRACT_RULE_PARTICIPANTS)
            }
        }
    }

    @Test
    fun `On relationship attestation issuance, all relationship participants must be included (decentralized)`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(DECENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                output(RelationshipAttestationContract.ID, attestation.copy(attestees = setOf(IDENTITY_B.party)))
                reference(relationship.ref)
                command(keysOf(IDENTITY_A), RelationshipAttestationContract.Issue)
                failsWith(RelationshipAttestationContract.Issue.CONTRACT_RULE_PARTICIPANTS)
            }
        }
    }

    @Test
    fun `On relationship attestation issuance, the attestation and relationship network hash must be equal (centralized)`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(CENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                output(RelationshipAttestationContract.ID, attestation.copy(network = INVALID_NETWORK))
                reference(relationship.ref)
                command(keysOf(IDENTITY_A), RelationshipAttestationContract.Issue)
                failsWith(RelationshipAttestationContract.Issue.CONTRACT_RULE_NETWORK_HASH)
            }
        }
    }

    @Test
    fun `On relationship attestation issuance, the attestation and relationship network hash must be equal (decentralized)`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(DECENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                output(RelationshipAttestationContract.ID, attestation.copy(network = INVALID_NETWORK))
                reference(relationship.ref)
                command(keysOf(IDENTITY_A), RelationshipAttestationContract.Issue)
                failsWith(RelationshipAttestationContract.Issue.CONTRACT_RULE_NETWORK_HASH)
            }
        }
    }

    @Test
    fun `On relationship attestation issuance, only the attestor must sign the transaction (centralized)`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(CENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                output(RelationshipAttestationContract.ID, attestation)
                reference(relationship.ref)
                command(keysOf(IDENTITY_B), RelationshipAttestationContract.Issue)
                failsWith(RelationshipAttestationContract.Issue.CONTRACT_RULE_SIGNERS)
            }
        }
    }

    @Test
    fun `On relationship attestation issuance, only the attestor must sign the transaction (decentralized)`() {
        services.ledger {
            transaction {
                val (relationship, attestation) = initialize(DECENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                output(RelationshipAttestationContract.ID, attestation)
                reference(relationship.ref)
                command(keysOf(IDENTITY_B), RelationshipAttestationContract.Issue)
                failsWith(RelationshipAttestationContract.Issue.CONTRACT_RULE_SIGNERS)
            }
        }
    }
}