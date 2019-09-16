package io.cordacity.koncerto.contract.membership.centralized

import io.cordacity.koncerto.contract.*
import io.cordacity.koncerto.contract.DummyIdentity.Companion.DUMMY_IDENTITY_B
import io.cordacity.koncerto.contract.membership.MembershipContract
import net.corda.testing.node.ledger
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Centralized membership contract amendment tests")
class MembershipContractAmendmentTests : ContractTest() {

    @Test
    fun `On membership amendment, the transaction must include the Amend command`() {
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
    fun `On membership amendment, only one state must be consumed`() {
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
    fun `On membership amendment, only one state must be created`() {
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
    fun `On membership amendment, the network hash must not change`() {
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
    fun `On membership amendment, the network identity must not change`() {
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
    fun `On membership amendment, network operators must possess the network operator role`() {
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
    fun `On membership amendment, the previous state reference must be equal to the input state reference`() {
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
    fun `On membership amendment, either the network member or the network operator must sign the transaction`() {
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
}