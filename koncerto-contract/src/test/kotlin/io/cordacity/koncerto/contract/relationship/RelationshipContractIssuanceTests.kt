package io.cordacity.koncerto.contract.relationship

import io.cordacity.koncerto.contract.*
import io.cordacity.koncerto.contract.revocation.RevocationLockContract
import io.cordacity.koncerto.contract.revocation.RevocationLockState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class RelationshipContractIssuanceTests : ContractTest() {

    @Test
    fun `On relationship issuance, the transaction must include the Issue command (centralized)`() {
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
    fun `On relationship issuance, the transaction must include the Issue command (decentralized)`() {
        services.ledger {
            transaction {
                output(RelationshipContract.ID, DECENTRALIZED_RELATIONSHIP)
                partiesOf(IDENTITY_A, IDENTITY_B, IDENTITY_C).forEach {
                    output(RevocationLockContract.ID, RevocationLockState.create(it, DECENTRALIZED_RELATIONSHIP))
                }
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C), RevocationLockContract.Create)
                fails()
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C), RelationshipContract.Issue)
                verifies()
            }
        }
    }

    @Test
    fun `On relationship issuance, zero states must be consumed (centralized)`() {
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
    fun `On relationship issuance, zero states must be consumed (decentralized)`() {
        services.ledger {
            transaction {
                input(RelationshipContract.ID, DECENTRALIZED_RELATIONSHIP)
                output(RelationshipContract.ID, DECENTRALIZED_RELATIONSHIP)
                partiesOf(IDENTITY_A, IDENTITY_B, IDENTITY_C).forEach {
                    output(RevocationLockContract.ID, RevocationLockState.create(it, DECENTRALIZED_RELATIONSHIP))
                }
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C), RevocationLockContract.Create)
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C), RelationshipContract.Issue)
                failsWith(RelationshipContract.Issue.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On relationship issuance, only one relationship state must be created (centralized)`() {
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
    fun `On relationship issuance, only one relationship state must be created (decentralized)`() {
        services.ledger {
            transaction {
                output(RelationshipContract.ID, DECENTRALIZED_RELATIONSHIP)
                output(RelationshipContract.ID, DECENTRALIZED_RELATIONSHIP)
                partiesOf(IDENTITY_A, IDENTITY_B, IDENTITY_C).forEach {
                    output(RevocationLockContract.ID, RevocationLockState.create(it, DECENTRALIZED_RELATIONSHIP))
                }
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C), RevocationLockContract.Create)
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C), RelationshipContract.Issue)
                failsWith(RelationshipContract.Issue.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On relationship issuance, revocation locks must be issued for all participants (centralized)`() {
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
    fun `On relationship issuance, revocation locks must be issued for all participants (decentralized)`() {
        services.ledger {
            transaction {
                output(RelationshipContract.ID, DECENTRALIZED_RELATIONSHIP)
                partiesOf(IDENTITY_A, IDENTITY_B).forEach {
                    output(RevocationLockContract.ID, RevocationLockState.create(it, DECENTRALIZED_RELATIONSHIP))
                }
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C), RevocationLockContract.Create)
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C), RelationshipContract.Issue)
                failsWith(RelationshipContract.Issue.CONTRACT_RULE_LOCKS)
            }
        }
    }

    @Test
    fun `On relationship issuance, all revocation locks must point to the relationship state (centralized)`() {
        services.ledger {
            transaction {
                output(RelationshipContract.ID, CENTRALIZED_RELATIONSHIP.copy(linearId = UniqueIdentifier()))
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
    fun `On relationship issuance, all revocation locks must point to the relationship state (decentralized)`() {
        services.ledger {
            transaction {
                output(RelationshipContract.ID, DECENTRALIZED_RELATIONSHIP.copy(linearId = UniqueIdentifier()))
                partiesOf(IDENTITY_A, IDENTITY_B, IDENTITY_C).forEach {
                    output(RevocationLockContract.ID, RevocationLockState.create(it, DECENTRALIZED_RELATIONSHIP))
                }
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C), RevocationLockContract.Create)
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C), RelationshipContract.Issue)
                failsWith(RelationshipContract.Issue.CONTRACT_REVOCATION_LOCK_POINTERS)
            }
        }
    }

    @Test
    fun `On relationship issuance, the previous state reference must be null (centralized)`() {
        services.ledger {
            transaction {
                output(RelationshipContract.ID, CENTRALIZED_RELATIONSHIP.copy(previousStateRef = INVALID_STATEREF))
                partiesOf(IDENTITY_A, IDENTITY_B, IDENTITY_C, OPERATOR_A).forEach {
                    output(RevocationLockContract.ID, RevocationLockState.create(it, CENTRALIZED_RELATIONSHIP))
                }
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C, OPERATOR_A), RevocationLockContract.Create)
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C, OPERATOR_A), RelationshipContract.Issue)
                failsWith(RelationshipContract.Issue.CONTRACT_RULE_PREVIOUS_REF)
            }
        }
    }

    @Test
    fun `On relationship issuance, the previous state reference must be null (decentralized)`() {
        services.ledger {
            transaction {
                output(RelationshipContract.ID, DECENTRALIZED_RELATIONSHIP.copy(previousStateRef = INVALID_STATEREF))
                partiesOf(IDENTITY_A, IDENTITY_B, IDENTITY_C).forEach {
                    output(RevocationLockContract.ID, RevocationLockState.create(it, DECENTRALIZED_RELATIONSHIP))
                }
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C), RevocationLockContract.Create)
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C), RelationshipContract.Issue)
                failsWith(RelationshipContract.Issue.CONTRACT_RULE_PREVIOUS_REF)
            }
        }
    }

    @Test
    fun `On relationship issuance, all participants must sign the transaction (IDENTITY_A must sign) (centralized)`() {
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
    fun `On relationship issuance, all participants must sign the transaction (IDENTITY_A must sign) (decentralized)`() {
        services.ledger {
            transaction {
                output(RelationshipContract.ID, DECENTRALIZED_RELATIONSHIP)
                partiesOf(IDENTITY_A, IDENTITY_B, IDENTITY_C).forEach {
                    output(RevocationLockContract.ID, RevocationLockState.create(it, DECENTRALIZED_RELATIONSHIP))
                }
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C), RevocationLockContract.Create)
                command(keysOf(IDENTITY_B, IDENTITY_C), RelationshipContract.Issue)
                failsWith(RelationshipContract.Issue.CONTRACT_RULE_SIGNERS)
            }
        }
    }

    @Test
    fun `On relationship issuance, all participants must sign the transaction (IDENTITY_B must sign) (centralized)`() {
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
    fun `On relationship issuance, all participants must sign the transaction (IDENTITY_B must sign) (decentralized)`() {
        services.ledger {
            transaction {
                output(RelationshipContract.ID, DECENTRALIZED_RELATIONSHIP)
                partiesOf(IDENTITY_A, IDENTITY_B, IDENTITY_C).forEach {
                    output(RevocationLockContract.ID, RevocationLockState.create(it, DECENTRALIZED_RELATIONSHIP))
                }
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C), RevocationLockContract.Create)
                command(keysOf(IDENTITY_A, IDENTITY_C), RelationshipContract.Issue)
                failsWith(RelationshipContract.Issue.CONTRACT_RULE_SIGNERS)
            }
        }
    }

    @Test
    fun `On relationship issuance, all participants must sign the transaction (IDENTITY_C must sign) (centralized)`() {
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
    fun `On relationship issuance, all participants must sign the transaction (IDENTITY_C must sign) (decentralized)`() {
        services.ledger {
            transaction {
                output(RelationshipContract.ID, DECENTRALIZED_RELATIONSHIP)
                partiesOf(IDENTITY_A, IDENTITY_B, IDENTITY_C).forEach {
                    output(RevocationLockContract.ID, RevocationLockState.create(it, DECENTRALIZED_RELATIONSHIP))
                }
                command(keysOf(IDENTITY_A, IDENTITY_B, IDENTITY_C), RevocationLockContract.Create)
                command(keysOf(IDENTITY_A, IDENTITY_B), RelationshipContract.Issue)
                failsWith(RelationshipContract.Issue.CONTRACT_RULE_SIGNERS)
            }
        }
    }

    @Test
    fun `On relationship issuance, all participants must sign the transaction (OPERATOR_A must sign) (centralized)`() {
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