package io.cordacity.koncerto.contract.relationship.centralized

import io.cordacity.koncerto.contract.*
import io.cordacity.koncerto.contract.relationship.RelationshipContract
import net.corda.testing.node.ledger
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Centralized relationship contract amendment tests")
class RelationshipContractAmendmentTests : ContractTest() {

    @Test
    fun `On relationship amendment, the transaction must include the Amend command`() {
        services.ledger {
            transaction {
                val (input, output) = initialize(CENTRALIZED_RELATIONSHIP)
                input(input.ref)
                output(RelationshipContract.ID, output)
                fails()
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C, OPERATOR_A), RelationshipContract.Amend)
                verifies()
            }
        }
    }

    @Test
    fun `On relationship amendment, only one state must be consumed`() {
        services.ledger {
            transaction {
                val (input, output) = initialize(CENTRALIZED_RELATIONSHIP)
                input(input.ref)
                input(createDummyOutput().ref)
                output(RelationshipContract.ID, output)
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C, OPERATOR_A), RelationshipContract.Amend)
                failsWith(RelationshipContract.Amend.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On relationship amendment, only one state must be created`() {
        services.ledger {
            transaction {
                val (input, output) = initialize(CENTRALIZED_RELATIONSHIP)
                input(input.ref)
                output(RelationshipContract.ID, output)
                output(RelationshipContract.ID, output)
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C, OPERATOR_A), RelationshipContract.Amend)
                failsWith(RelationshipContract.Amend.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On relationship amendment, the network hash must not change`() {
        services.ledger {
            transaction {
                val (input, output) = initialize(CENTRALIZED_RELATIONSHIP)
                input(input.ref)
                output(RelationshipContract.ID, output.copy(network = INVALID_NETWORK))
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C, OPERATOR_A), RelationshipContract.Amend)
                failsWith(RelationshipContract.Amend.CONTRACT_RULE_NETWORK_HASH)
            }
        }
    }

    @Test
    fun `On relationship amendment, the previous state reference must be equal to the input state reference`() {
        services.ledger {
            transaction {
                val (input, output) = initialize(CENTRALIZED_RELATIONSHIP)
                input(input.ref)
                output(RelationshipContract.ID, output.copy(previousStateRef = INVALID_STATEREF))
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C, OPERATOR_A), RelationshipContract.Amend)
                failsWith(RelationshipContract.Amend.CONTRACT_RULE_PREVIOUS_REF)
            }
        }
    }

    @Test
    fun `On relationship amendment, all participants must sign the transaction (IDENTITY_A must sign)`() {
        services.ledger {
            transaction {
                val (input, output) = initialize(CENTRALIZED_RELATIONSHIP)
                input(input.ref)
                output(RelationshipContract.ID, output)
                command(keysOf(IDENTITY_B, IDENTITY_C, OPERATOR_A), RelationshipContract.Amend)
                failsWith(RelationshipContract.Amend.CONTRACT_RULE_SIGNERS)
            }
        }
    }

    @Test
    fun `On relationship amendment, all participants must sign the transaction (IDENTITY_B must sign)`() {
        services.ledger {
            transaction {
                val (input, output) = initialize(CENTRALIZED_RELATIONSHIP)
                input(input.ref)
                output(RelationshipContract.ID, output)
                command(keysOf(IDENTITY_A, IDENTITY_C, OPERATOR_A), RelationshipContract.Amend)
                failsWith(RelationshipContract.Amend.CONTRACT_RULE_SIGNERS)
            }
        }
    }

    @Test
    fun `On relationship amendment, all participants must sign the transaction (IDENTITY_C must sign)`() {
        services.ledger {
            transaction {
                val (input, output) = initialize(CENTRALIZED_RELATIONSHIP)
                input(input.ref)
                output(RelationshipContract.ID, output)
                command(keysOf(IDENTITY_A, IDENTITY_B, OPERATOR_A), RelationshipContract.Amend)
                failsWith(RelationshipContract.Amend.CONTRACT_RULE_SIGNERS)
            }
        }
    }

    @Test
    fun `On relationship amendment, all participants must sign the transaction (OPERATOR_A must sign)`() {
        services.ledger {
            transaction {
                val (input, output) = initialize(CENTRALIZED_RELATIONSHIP)
                input(input.ref)
                output(RelationshipContract.ID, output)
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C), RelationshipContract.Amend)
                failsWith(RelationshipContract.Amend.CONTRACT_RULE_SIGNERS)
            }
        }
    }
}