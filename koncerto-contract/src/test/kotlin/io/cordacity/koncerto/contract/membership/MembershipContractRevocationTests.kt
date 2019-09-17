package io.cordacity.koncerto.contract.membership

import io.cordacity.koncerto.contract.*
import io.cordacity.koncerto.contract.membership.MembershipContract
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class MembershipContractRevocationTests : ContractTest() {

    @Test
    fun `On membership revocation, the transaction must include the Revoke command (centralized)`() {
        services.ledger {
            transaction {
                input(MembershipContract.ID, CENTRALIZED_MEMBERSHIP_A)
                fails()
                command(keysOf(IDENTITY_A), MembershipContract.Revoke)
                verifies()
            }
        }
    }

    @Test
    fun `On membership revocation, the transaction must include the Revoke command (decentralized)`() {
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
    fun `On membership revocation, only one state must be consumed (centralized)`() {
        services.ledger {
            transaction {
                input(MembershipContract.ID, CENTRALIZED_MEMBERSHIP_A)
                input(MembershipContract.ID, CENTRALIZED_MEMBERSHIP_A)
                command(keysOf(IDENTITY_A), MembershipContract.Revoke)
                failsWith(MembershipContract.Revoke.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On membership revocation, only one state must be consumed (decentralized)`() {
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
    fun `On membership revocation, zero states must be created (centralized)`() {
        services.ledger {
            transaction {
                input(MembershipContract.ID, CENTRALIZED_MEMBERSHIP_A)
                output(MembershipContract.ID, CENTRALIZED_MEMBERSHIP_A)
                command(keysOf(IDENTITY_A), MembershipContract.Revoke)
                failsWith(MembershipContract.Revoke.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On membership revocation, zero states must be created (decentralized)`() {
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
    fun `On membership revocation, either the network member or the network operator must sign the transaction (centralized)`() {
        services.ledger {
            transaction {
                input(MembershipContract.ID, CENTRALIZED_MEMBERSHIP_A)
                command(keysOf(IDENTITY_B), MembershipContract.Revoke)
                failsWith(MembershipContract.Revoke.CONTRACT_RULE_SIGNERS)
            }
        }
    }

    @Test
    fun `On membership revocation, either the network member or the network operator must sign the transaction (decentralized)`() {
        services.ledger {
            transaction {
                input(MembershipContract.ID, DECENTRALIZED_MEMBERSHIP_A)
                command(keysOf(IDENTITY_B), MembershipContract.Revoke)
                failsWith(MembershipContract.Revoke.CONTRACT_RULE_SIGNERS)
            }
        }
    }
}