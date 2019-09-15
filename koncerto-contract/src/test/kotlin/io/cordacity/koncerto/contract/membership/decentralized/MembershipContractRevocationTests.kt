package io.cordacity.koncerto.contract.membership.decentralized

import io.cordacity.koncerto.contract.ContractTest
import io.cordacity.koncerto.contract.DECENTRALIZED_MEMBERSHIP_A
import io.cordacity.koncerto.contract.IDENTITY_A
import io.cordacity.koncerto.contract.IDENTITY_B
import io.cordacity.koncerto.contract.membership.MembershipContract
import net.corda.testing.node.ledger
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Centralized membership contract revocation tests")
class MembershipContractRevocationTests : ContractTest() {

    @Test
    fun `On membership revocation, the transaction must include the Revoke command`() {
        services.ledger {
            transaction {
                input(MembershipContract.ID, DECENTRALIZED_MEMBERSHIP_A)
                fails()
                command(keysOf(IDENTITY_A), MembershipContract.Revoke)
                verifies()
            }
        }
    }

    @Test
    fun `On membership revocation, only one state must be consumed`() {
        services.ledger {
            transaction {
                input(MembershipContract.ID, DECENTRALIZED_MEMBERSHIP_A)
                input(MembershipContract.ID, DECENTRALIZED_MEMBERSHIP_A)
                command(keysOf(IDENTITY_A), MembershipContract.Revoke)
                failsWith(MembershipContract.Revoke.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On membership revocation, zero states must be created`() {
        services.ledger {
            transaction {
                input(MembershipContract.ID, DECENTRALIZED_MEMBERSHIP_A)
                output(MembershipContract.ID, DECENTRALIZED_MEMBERSHIP_A)
                command(keysOf(IDENTITY_A), MembershipContract.Revoke)
                failsWith(MembershipContract.Revoke.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On membership revocation, either the network member or the network operator must sign the transaction`() {
        services.ledger {
            transaction {
                input(MembershipContract.ID, DECENTRALIZED_MEMBERSHIP_A)
                command(keysOf(IDENTITY_B), MembershipContract.Revoke)
                failsWith(MembershipContract.Revoke.CONTRACT_RULE_SIGNERS)
            }
        }
    }
}