package io.cordacity.koncerto.contract.membership

import io.cordacity.koncerto.contract.*
import io.cordacity.koncerto.contract.DummyIdentity.Companion.DUMMY_IDENTITY_B
import io.cordacity.koncerto.contract.membership.MembershipContract
import net.corda.testing.node.ledger
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Centralized membership contract amendment tests")
class MembershipContractAmendmentTests : ContractTest() {

    @Test
    fun `On membership amendment, the transaction must include the Amend command (centralized)`() {
        services.ledger {
            transaction {
                val (input, output) = initialize(CENTRALIZED_MEMBERSHIP_A)
                input(input.ref)
                output(MembershipContract.ID, output)
                fails()
                command(keysOf(IDENTITY_A), MembershipContract.Amend)
                verifies()
            }
        }
    }

    @Test
    fun `On membership amendment, the transaction must include the Amend command (decentralized)`() {
        services.ledger {
            transaction {
                val (input, output) = initialize(DECENTRALIZED_MEMBERSHIP_A)
                input(input.ref)
                output(MembershipContract.ID, output)
                fails()
                command(keysOf(IDENTITY_A), MembershipContract.Amend)
                verifies()
            }
        }
    }

    @Test
    fun `On membership amendment, only one state must be consumed (centralized)`() {
        services.ledger {
            transaction {
                val (input, output) = initialize(CENTRALIZED_MEMBERSHIP_A)
                input(input.ref)
                input(createDummyOutput().ref)
                output(MembershipContract.ID, output)
                command(keysOf(IDENTITY_A), MembershipContract.Amend)
                failsWith(MembershipContract.Amend.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On membership amendment, only one state must be consumed (decentralized)`() {
        services.ledger {
            transaction {
                val (input, output) = initialize(DECENTRALIZED_MEMBERSHIP_A)
                input(input.ref)
                input(createDummyOutput().ref)
                output(MembershipContract.ID, output)
                command(keysOf(IDENTITY_A), MembershipContract.Amend)
                failsWith(MembershipContract.Amend.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On membership amendment, only one state must be created (centralized)`() {
        services.ledger {
            transaction {
                val (input, output) = initialize(CENTRALIZED_MEMBERSHIP_A)
                input(input.ref)
                output(MembershipContract.ID, output)
                output(MembershipContract.ID, output)
                command(keysOf(IDENTITY_A), MembershipContract.Amend)
                failsWith(MembershipContract.Amend.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On membership amendment, only one state must be created (decentralized)`() {
        services.ledger {
            transaction {
                val (input, output) = initialize(DECENTRALIZED_MEMBERSHIP_A)
                input(input.ref)
                output(MembershipContract.ID, output)
                output(MembershipContract.ID, output)
                command(keysOf(IDENTITY_A), MembershipContract.Amend)
                failsWith(MembershipContract.Amend.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On membership amendment, the network hash must not change (centralized)`() {
        services.ledger {
            transaction {
                val (input, output) = initialize(CENTRALIZED_MEMBERSHIP_A)
                input(input.ref)
                output(MembershipContract.ID, output.copy(network = INVALID_NETWORK))
                command(keysOf(IDENTITY_A), MembershipContract.Amend)
                failsWith(MembershipContract.Amend.CONTRACT_RULE_NETWORK_HASH)
            }
        }
    }

    @Test
    fun `On membership amendment, the network hash must not change (decentralized)`() {
        services.ledger {
            transaction {
                val (input, output) = initialize(DECENTRALIZED_MEMBERSHIP_A)
                input(input.ref)
                output(MembershipContract.ID, output.copy(network = INVALID_NETWORK))
                command(keysOf(IDENTITY_A), MembershipContract.Amend)
                failsWith(MembershipContract.Amend.CONTRACT_RULE_NETWORK_HASH)
            }
        }
    }

    @Test
    fun `On membership amendment, the network identity must not change (centralized)`() {
        services.ledger {
            transaction {
                val (input, output) = initialize(CENTRALIZED_MEMBERSHIP_A)
                input(input.ref)
                output(MembershipContract.ID, output.copy(identity = DUMMY_IDENTITY_B))
                command(keysOf(IDENTITY_A), MembershipContract.Amend)
                failsWith(MembershipContract.Amend.CONTRACT_RULE_NETWORK_IDENTITY)
            }
        }
    }

    @Test
    fun `On membership amendment, the network identity must not change (decentralized)`() {
        services.ledger {
            transaction {
                val (input, output) = initialize(DECENTRALIZED_MEMBERSHIP_A)
                input(input.ref)
                output(MembershipContract.ID, output.copy(identity = DUMMY_IDENTITY_B))
                command(keysOf(IDENTITY_A), MembershipContract.Amend)
                failsWith(MembershipContract.Amend.CONTRACT_RULE_NETWORK_IDENTITY)
            }
        }
    }

    @Test
    fun `On membership amendment, network operators must possess the network operator role (centralized)`() {
        services.ledger {
            transaction {
                val (input, output) = initialize(CENTRALIZED_MEMBERSHIP_O)
                input(input.ref)
                output(MembershipContract.ID, output.copy(roles = emptySet()))
                command(keysOf(OPERATOR_A), MembershipContract.Amend)
                failsWith(MembershipContract.Amend.CONTRACT_RULE_ROLES)
            }
        }
    }

    @Test
    fun `On membership amendment, the previous state reference must be equal to the input state reference (centralized)`() {
        services.ledger {
            transaction {
                val (input, output) = initialize(CENTRALIZED_MEMBERSHIP_A)
                input(input.ref)
                output(MembershipContract.ID, output.copy(previousStateRef = INVALID_STATEREF))
                command(keysOf(OPERATOR_A), MembershipContract.Amend)
                failsWith(MembershipContract.Amend.CONTRACT_RULE_PREVIOUS_REF)
            }
        }
    }

    @Test
    fun `On membership amendment, the previous state reference must be equal to the input state reference (decentralized)`() {
        services.ledger {
            transaction {
                val (input, output) = initialize(DECENTRALIZED_MEMBERSHIP_A)
                input(input.ref)
                output(MembershipContract.ID, output.copy(previousStateRef = INVALID_STATEREF))
                command(keysOf(IDENTITY_A), MembershipContract.Amend)
                failsWith(MembershipContract.Amend.CONTRACT_RULE_PREVIOUS_REF)
            }
        }
    }

    @Test
    fun `On membership amendment, either the network member or the network operator must sign the transaction (centralized)`() {
        services.ledger {
            transaction {
                val (input, output) = initialize(CENTRALIZED_MEMBERSHIP_A)
                input(input.ref)
                output(MembershipContract.ID, output)
                command(keysOf(IDENTITY_B), MembershipContract.Amend)
                failsWith(MembershipContract.Amend.CONTRACT_RULE_SIGNERS)
            }
        }
    }

    @Test
    fun `On membership amendment, either the network member or the network operator must sign the transaction (decentralized)`() {
        services.ledger {
            transaction {
                val (input, output) = initialize(DECENTRALIZED_MEMBERSHIP_A)
                input(input.ref)
                output(MembershipContract.ID, output)
                command(keysOf(IDENTITY_B), MembershipContract.Amend)
                failsWith(MembershipContract.Amend.CONTRACT_RULE_SIGNERS)
            }
        }
    }
}