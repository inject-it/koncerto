package io.cordacity.koncerto.contract.relationship.centralized

import io.cordacity.koncerto.contract.*
import io.cordacity.koncerto.contract.relationship.RelationshipContract
import io.cordacity.koncerto.contract.revocation.RevocationLockContract
import io.cordacity.koncerto.contract.revocation.RevocationLockState
import net.corda.testing.node.ledger
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Centralized relationship contract issuance tests")
class RelationshipContractIssuanceTests : ContractTest() {

    @Test
    fun `On relationship issuance, the transaction must include the Issue command`() {
        services.ledger {
            transaction {
                output(RelationshipContract.ID, CENTRALIZED_RELATIONSHIP)
                partiesOf(IDENTITY_A, IDENTITY_B, IDENTITY_C, OPERATOR_A).forEach {
                    output(RevocationLockContract.ID, RevocationLockState.create(it, CENTRALIZED_RELATIONSHIP))
                }
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C, OPERATOR_A), RevocationLockContract.Create)
                fails()
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C, OPERATOR_A), RelationshipContract.Issue)
                verifies()
            }
        }
    }

    @Test
    fun `On relationship issuance, zero states must be consumed`() {
        services.ledger {
            transaction {
                input(RelationshipContract.ID, CENTRALIZED_RELATIONSHIP)
                output(RelationshipContract.ID, CENTRALIZED_RELATIONSHIP)
                partiesOf(IDENTITY_A, IDENTITY_B, IDENTITY_C, OPERATOR_A).forEach {
                    output(RevocationLockContract.ID, RevocationLockState.create(it, CENTRALIZED_RELATIONSHIP))
                }
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C, OPERATOR_A), RevocationLockContract.Create)
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C, OPERATOR_A), RelationshipContract.Issue)
                failsWith(RelationshipContract.Issue.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On relationship issuance, only one relationship state must be created`() {
        services.ledger {
            transaction {
                output(RelationshipContract.ID, CENTRALIZED_RELATIONSHIP)
                output(RelationshipContract.ID, CENTRALIZED_RELATIONSHIP)
                partiesOf(IDENTITY_A, IDENTITY_B, IDENTITY_C, OPERATOR_A).forEach {
                    output(RevocationLockContract.ID, RevocationLockState.create(it, CENTRALIZED_RELATIONSHIP))
                }
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C, OPERATOR_A), RevocationLockContract.Create)
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C, OPERATOR_A), RelationshipContract.Issue)
                failsWith(RelationshipContract.Issue.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On relationship issuance, revocation locks must be issued for all participants`() {
        services.ledger {
            transaction {
                output(RelationshipContract.ID, CENTRALIZED_RELATIONSHIP)
                partiesOf(IDENTITY_A, IDENTITY_B, IDENTITY_C).forEach {
                    output(RevocationLockContract.ID, RevocationLockState.create(it, CENTRALIZED_RELATIONSHIP))
                }
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C, OPERATOR_A), RevocationLockContract.Create)
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C, OPERATOR_A), RelationshipContract.Issue)
                failsWith(RelationshipContract.Issue.CONTRACT_RULE_LOCKS)
            }
        }
    }

    @Test
    fun `On relationship issuance, all revocation locks must point to the relationship state`() {
        services.ledger {
            transaction {
                output(RelationshipContract.ID, CENTRALIZED_RELATIONSHIP.copy(network = INVALID_NETWORK))
                partiesOf(IDENTITY_A, IDENTITY_B, IDENTITY_C, OPERATOR_A).forEach {
                    output(RevocationLockContract.ID, RevocationLockState.create(it, CENTRALIZED_RELATIONSHIP))
                }
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C, OPERATOR_A), RevocationLockContract.Create)
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C, OPERATOR_A), RelationshipContract.Issue)
                failsWith(RelationshipContract.Issue.CONTRACT_REVOCATION_LOCK_POINTERS)
            }
        }
    }

    @Test
    fun `On relationship issuance, all participants must sign the transaction (IDENTITY_A must sign)`() {
        services.ledger {
            transaction {
                output(RelationshipContract.ID, CENTRALIZED_RELATIONSHIP)
                partiesOf(IDENTITY_A, IDENTITY_B, IDENTITY_C, OPERATOR_A).forEach {
                    output(RevocationLockContract.ID, RevocationLockState.create(it, CENTRALIZED_RELATIONSHIP))
                }
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C, OPERATOR_A), RevocationLockContract.Create)
                command(keysOf(IDENTITY_B, IDENTITY_C, OPERATOR_A), RelationshipContract.Issue)
                failsWith(RelationshipContract.Issue.CONTRACT_RULE_SIGNERS)
            }
        }
    }

    @Test
    fun `On relationship issuance, all participants must sign the transaction (IDENTITY_B must sign)`() {
        services.ledger {
            transaction {
                output(RelationshipContract.ID, CENTRALIZED_RELATIONSHIP)
                partiesOf(IDENTITY_A, IDENTITY_B, IDENTITY_C, OPERATOR_A).forEach {
                    output(RevocationLockContract.ID, RevocationLockState.create(it, CENTRALIZED_RELATIONSHIP))
                }
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C, OPERATOR_A), RevocationLockContract.Create)
                command(keysOf(IDENTITY_A, IDENTITY_C, OPERATOR_A), RelationshipContract.Issue)
                failsWith(RelationshipContract.Issue.CONTRACT_RULE_SIGNERS)
            }
        }
    }

    @Test
    fun `On relationship issuance, all participants must sign the transaction (IDENTITY_C must sign)`() {
        services.ledger {
            transaction {
                output(RelationshipContract.ID, CENTRALIZED_RELATIONSHIP)
                partiesOf(IDENTITY_A, IDENTITY_B, IDENTITY_C, OPERATOR_A).forEach {
                    output(RevocationLockContract.ID, RevocationLockState.create(it, CENTRALIZED_RELATIONSHIP))
                }
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C, OPERATOR_A), RevocationLockContract.Create)
                command(keysOf(IDENTITY_A, IDENTITY_B, OPERATOR_A), RelationshipContract.Issue)
                failsWith(RelationshipContract.Issue.CONTRACT_RULE_SIGNERS)
            }
        }
    }

    @Test
    fun `On relationship issuance, all participants must sign the transaction (OPERATOR_A must sign)`() {
        services.ledger {
            transaction {
                output(RelationshipContract.ID, CENTRALIZED_RELATIONSHIP)
                partiesOf(IDENTITY_A, IDENTITY_B, IDENTITY_C, OPERATOR_A).forEach {
                    output(RevocationLockContract.ID, RevocationLockState.create(it, CENTRALIZED_RELATIONSHIP))
                }
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C, OPERATOR_A), RevocationLockContract.Create)
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C), RelationshipContract.Issue)
                failsWith(RelationshipContract.Issue.CONTRACT_RULE_SIGNERS)
            }
        }
    }
}