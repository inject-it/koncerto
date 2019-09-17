package io.cordacity.koncerto.contract.relationship

import io.cordacity.koncerto.contract.*
import io.cordacity.koncerto.contract.relationship.RelationshipAttestationContract
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class RelationshipAttestationContractRevocationTests : ContractTest() {

    @Test
    fun `On relationship attestation amendment, the transaction must include the Revoke command (centralized)`() {
        services.ledger {
            transaction {
                val (_, attestation) = initialize(CENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                input(RelationshipAttestationContract.ID, attestation)
                fails()
                command(keysOf(IDENTITY_A), RelationshipAttestationContract.Revoke)
                verifies()
            }
        }
    }

    @Test
    fun `On relationship attestation amendment, the transaction must include the Revoke command (decentralized)`() {
        services.ledger {
            transaction {
                val (_, attestation) = initialize(DECENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                input(RelationshipAttestationContract.ID, attestation)
                fails()
                command(keysOf(IDENTITY_A), RelationshipAttestationContract.Revoke)
                verifies()
            }
        }
    }

    @Test
    fun `On relationship attestation revocation, only one state must be consumed (centralized)`() {
        services.ledger {
            transaction {
                val (_, attestation) = initialize(CENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                output(RelationshipAttestationContract.ID, attestation)
                command(keysOf(IDENTITY_A), RelationshipAttestationContract.Revoke)
                failsWith(RelationshipAttestationContract.Revoke.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On relationship attestation revocation, only one state must be consumed (decentralized)`() {
        services.ledger {
            transaction {
                val (_, attestation) = initialize(DECENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                output(RelationshipAttestationContract.ID, attestation)
                command(keysOf(IDENTITY_A), RelationshipAttestationContract.Revoke)
                failsWith(RelationshipAttestationContract.Revoke.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On relationship attestation revocation, zero states must be created (centralized)`() {
        services.ledger {
            transaction {
                val (_, attestation) = initialize(CENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                input(RelationshipAttestationContract.ID, attestation)
                output(RelationshipAttestationContract.ID, attestation)
                command(keysOf(IDENTITY_A), RelationshipAttestationContract.Revoke)
                failsWith(RelationshipAttestationContract.Revoke.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On relationship attestation revocation, zero states must be created (decentralized)`() {
        services.ledger {
            transaction {
                val (_, attestation) = initialize(DECENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                input(RelationshipAttestationContract.ID, attestation)
                output(RelationshipAttestationContract.ID, attestation)
                command(keysOf(IDENTITY_A), RelationshipAttestationContract.Revoke)
                failsWith(RelationshipAttestationContract.Revoke.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On relationship attestation revocation, only the attestor must sign the transaction (centralized)`() {
        services.ledger {
            transaction {
                val (_, attestation) = initialize(CENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                input(RelationshipAttestationContract.ID, attestation)
                command(keysOf(IDENTITY_B), RelationshipAttestationContract.Revoke)
                failsWith(RelationshipAttestationContract.Revoke.CONTRACT_RULE_SIGNERS)
            }
        }
    }

    @Test
    fun `On relationship attestation revocation, only the attestor must sign the transaction (decentralized)`() {
        services.ledger {
            transaction {
                val (_, attestation) = initialize(DECENTRALIZED_RELATIONSHIP, IDENTITY_A.party)
                input(RelationshipAttestationContract.ID, attestation)
                command(keysOf(IDENTITY_B), RelationshipAttestationContract.Revoke)
                failsWith(RelationshipAttestationContract.Revoke.CONTRACT_RULE_SIGNERS)
            }
        }
    }
}