package io.cordacity.koncerto.contract.relationship

import io.cordacity.koncerto.contract.*
import io.cordacity.koncerto.contract.relationship.RelationshipContract
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class RelationshipContractRevocationTests : ContractTest() {

    @Test
    fun `On relationship revocation, the transaction must include the Revoke command (centralized)`() {
        services.ledger {
            transaction {
                input(RelationshipContract.ID, CENTRALIZED_RELATIONSHIP)
                fails()
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C, OPERATOR_A), RelationshipContract.Revoke)
                verifies()
            }
        }
    }

    @Test
    fun `On relationship revocation, the transaction must include the Revoke command (decentralized)`() {
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
    fun `On relationship revocation, only one state must be consumed (centralized)`() {
        services.ledger {
            transaction {
                input(RelationshipContract.ID, CENTRALIZED_RELATIONSHIP)
                input(RelationshipContract.ID, CENTRALIZED_RELATIONSHIP)
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C, OPERATOR_A), RelationshipContract.Revoke)
                failsWith(RelationshipContract.Revoke.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On relationship revocation, only one state must be consumed (decentralized)`() {
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
    fun `On relationship revocation, zero states must be created (centralized)`() {
        services.ledger {
            transaction {
                input(RelationshipContract.ID, CENTRALIZED_RELATIONSHIP)
                output(RelationshipContract.ID, CENTRALIZED_RELATIONSHIP)
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C, OPERATOR_A), RelationshipContract.Revoke)
                failsWith(RelationshipContract.Revoke.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On relationship revocation, zero states must be created (decentralized)`() {
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
    fun `On relationship revocation, all participants must sign the transaction (IDENTITY_A must sign) (centralized)`() {
        services.ledger {
            transaction {
                input(RelationshipContract.ID, CENTRALIZED_RELATIONSHIP)
                command(keysOf(IDENTITY_B, IDENTITY_C, OPERATOR_A), RelationshipContract.Revoke)
                failsWith(RelationshipContract.Revoke.CONTRACT_RULE_SIGNERS)
            }
        }
    }

    @Test
    fun `On relationship revocation, all participants must sign the transaction (IDENTITY_A must sign) (decentralized)`() {
        services.ledger {
            transaction {
                input(RelationshipContract.ID, DECENTRALIZED_RELATIONSHIP)
                command(keysOf(IDENTITY_B, IDENTITY_C), RelationshipContract.Revoke)
                failsWith(RelationshipContract.Revoke.CONTRACT_RULE_SIGNERS)
            }
        }
    }

    @Test
    fun `On relationship revocation, all participants must sign the transaction (IDENTITY_B must sign) (centralized)`() {
        services.ledger {
            transaction {
                input(RelationshipContract.ID, CENTRALIZED_RELATIONSHIP)
                command(keysOf(IDENTITY_A, IDENTITY_C, OPERATOR_A), RelationshipContract.Revoke)
                failsWith(RelationshipContract.Revoke.CONTRACT_RULE_SIGNERS)
            }
        }
    }

    @Test
    fun `On relationship revocation, all participants must sign the transaction (IDENTITY_B must sign) (decentralized)`() {
        services.ledger {
            transaction {
                input(RelationshipContract.ID, DECENTRALIZED_RELATIONSHIP)
                command(keysOf(IDENTITY_A, IDENTITY_C), RelationshipContract.Revoke)
                failsWith(RelationshipContract.Revoke.CONTRACT_RULE_SIGNERS)
            }
        }
    }

    @Test
    fun `On relationship revocation, all participants must sign the transaction (IDENTITY_C must sign) (centralized)`() {
        services.ledger {
            transaction {
                input(RelationshipContract.ID, CENTRALIZED_RELATIONSHIP)
                command(keysOf(IDENTITY_A, IDENTITY_B, OPERATOR_A), RelationshipContract.Revoke)
                failsWith(RelationshipContract.Revoke.CONTRACT_RULE_SIGNERS)
            }
        }
    }

    @Test
    fun `On relationship revocation, all participants must sign the transaction (IDENTITY_C must sign) (decentralized)`() {
        services.ledger {
            transaction {
                input(RelationshipContract.ID, DECENTRALIZED_RELATIONSHIP)
                command(keysOf(IDENTITY_A, IDENTITY_B), RelationshipContract.Revoke)
                failsWith(RelationshipContract.Revoke.CONTRACT_RULE_SIGNERS)
            }
        }
    }

    @Test
    fun `On relationship revocation, all participants must sign the transaction (OPERATOR_A must sign) (centralized)`() {
        services.ledger {
            transaction {
                input(RelationshipContract.ID, CENTRALIZED_RELATIONSHIP)
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C), RelationshipContract.Revoke)
                failsWith(RelationshipContract.Revoke.CONTRACT_RULE_SIGNERS)
            }
        }
    }
}