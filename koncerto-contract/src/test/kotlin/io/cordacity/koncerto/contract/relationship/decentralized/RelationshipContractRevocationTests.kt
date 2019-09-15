package io.cordacity.koncerto.contract.relationship.decentralized

import io.cordacity.koncerto.contract.*
import io.cordacity.koncerto.contract.relationship.RelationshipContract
import net.corda.testing.node.ledger
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Decentralized relationship contract revocation tests")
class RelationshipContractRevocationTests : ContractTest() {

    @Test
    fun `On relationship revocation, the transaction must include the Revoke command`() {
        services.ledger {
            transaction {
                input(RelationshipContract.ID, DECENTRALIZED_RELATIONSHIP)
                fails()
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C), RelationshipContract.Revoke)
                verifies()
            }
        }
    }

    @Test
    fun `On relationship revocation, only one state must be consumed`() {
        services.ledger {
            transaction {
                input(RelationshipContract.ID, DECENTRALIZED_RELATIONSHIP)
                input(RelationshipContract.ID, DECENTRALIZED_RELATIONSHIP)
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C), RelationshipContract.Revoke)
                failsWith(RelationshipContract.Revoke.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On relationship revocation, zero states must be created`() {
        services.ledger {
            transaction {
                input(RelationshipContract.ID, DECENTRALIZED_RELATIONSHIP)
                output(RelationshipContract.ID, DECENTRALIZED_RELATIONSHIP)
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C), RelationshipContract.Revoke)
                failsWith(RelationshipContract.Revoke.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On relationship revocation, all participants must sign the transaction (IDENTITY_A must sign)`() {
        services.ledger {
            transaction {
                input(RelationshipContract.ID, DECENTRALIZED_RELATIONSHIP)
                command(keysOf(IDENTITY_B, IDENTITY_C), RelationshipContract.Revoke)
                failsWith(RelationshipContract.Revoke.CONTRACT_RULE_SIGNERS)
            }
        }
    }

    @Test
    fun `On relationship revocation, all participants must sign the transaction (IDENTITY_B must sign)`() {
        services.ledger {
            transaction {
                input(RelationshipContract.ID, DECENTRALIZED_RELATIONSHIP)
                command(keysOf(IDENTITY_A, IDENTITY_C), RelationshipContract.Revoke)
                failsWith(RelationshipContract.Revoke.CONTRACT_RULE_SIGNERS)
            }
        }
    }

    @Test
    fun `On relationship revocation, all participants must sign the transaction (IDENTITY_C must sign)`() {
        services.ledger {
            transaction {
                input(RelationshipContract.ID, DECENTRALIZED_RELATIONSHIP)
                command(keysOf(IDENTITY_A, IDENTITY_B), RelationshipContract.Revoke)
                failsWith(RelationshipContract.Revoke.CONTRACT_RULE_SIGNERS)
            }
        }
    }
}