package io.cordacity.koncerto.contract.relationship.decentralized

import io.cordacity.koncerto.contract.ContractTest
import io.cordacity.koncerto.contract.DECENTRALIZED_RELATIONSHIP
import io.cordacity.koncerto.contract.IDENTITY_A
import io.cordacity.koncerto.contract.IDENTITY_B
import io.cordacity.koncerto.contract.relationship.RelationshipAttestationContract
import net.corda.testing.node.ledger
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Decentralized relationship attestation contract revocation tests")
class RelationshipAttestationContractRevocationTests : ContractTest() {

    @Test
    fun `On relationship attestation amendment, the transaction must include the Revoke command`() {
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
    fun `On relationship attestation revocation, only one state must be consumed`() {
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
    fun `On relationship attestation revocation, zero states must be created`() {
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
    fun `On relationship attestation revocation, only the attestor must sign the transaction`() {
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