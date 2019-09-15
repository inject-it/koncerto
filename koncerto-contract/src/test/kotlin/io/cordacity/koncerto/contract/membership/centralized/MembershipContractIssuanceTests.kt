package io.cordacity.koncerto.contract.membership.centralized

import io.cordacity.koncerto.contract.*
import io.cordacity.koncerto.contract.membership.MembershipContract
import net.corda.testing.node.ledger
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Centralized membership contract issuance tests")
class MembershipContractIssuanceTests : ContractTest() {

    @Test
    fun `On membership issuance, the transaction must include the Issue command`() {
        services.ledger {
            transaction {
                output(MembershipContract.ID, CENTRALIZED_MEMBERSHIP_A)
                fails()
                command(keysOf(IDENTITY_A), MembershipContract.Issue)
                verifies()
            }
        }
    }

    @Test
    fun `On membership issuance, zero states must be consumed`() {
        services.ledger {
            transaction {
                input(MembershipContract.ID, CENTRALIZED_MEMBERSHIP_A)
                output(MembershipContract.ID, CENTRALIZED_MEMBERSHIP_A)
                command(keysOf(IDENTITY_A), MembershipContract.Issue)
                failsWith(MembershipContract.Issue.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On membership issuance, only one state must be created`() {
        services.ledger {
            transaction {
                output(MembershipContract.ID, CENTRALIZED_MEMBERSHIP_A)
                output(MembershipContract.ID, CENTRALIZED_MEMBERSHIP_A)
                command(keysOf(IDENTITY_A), MembershipContract.Issue)
                failsWith(MembershipContract.Issue.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On membership issuance, network operators must possess the network operator role`() {
        services.ledger {
            transaction {
                output(MembershipContract.ID, CENTRALIZED_MEMBERSHIP_O.copy(roles = emptySet()))
                command(keysOf(OPERATOR_A), MembershipContract.Issue)
                failsWith(MembershipContract.Issue.CONTRACT_RULE_ROLES)
            }
        }
    }

    @Test
    fun `On membership issuance, only the network member must sign the transaction`() {
        services.ledger {
            transaction {
                output(MembershipContract.ID, CENTRALIZED_MEMBERSHIP_A)
                command(keysOf(IDENTITY_B), MembershipContract.Issue)
                failsWith(MembershipContract.Issue.CONTRACT_RULE_SIGNERS)
            }
        }
    }
}