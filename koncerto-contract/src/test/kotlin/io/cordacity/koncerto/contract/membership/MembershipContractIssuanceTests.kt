package io.cordacity.koncerto.contract.membership

import io.cordacity.koncerto.contract.*
import io.cordacity.koncerto.contract.membership.MembershipContract
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class MembershipContractIssuanceTests : ContractTest() {

    @Test
    fun `On membership issuance, the transaction must include the Issue command (centralized)`() {
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
    fun `On membership issuance, the transaction must include the Issue command (decentralized)`() {
        services.ledger {
            transaction {
                output(MembershipContract.ID, DECENTRALIZED_MEMBERSHIP_A)
                fails()
                command(keysOf(IDENTITY_A), MembershipContract.Issue)
                verifies()
            }
        }
    }

    @Test
    fun `On membership issuance, zero states must be consumed (centralized)`() {
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
    fun `On membership issuance, zero states must be consumed (decentralized)`() {
        services.ledger {
            transaction {
                input(MembershipContract.ID, DECENTRALIZED_MEMBERSHIP_A)
                output(MembershipContract.ID, DECENTRALIZED_MEMBERSHIP_A)
                command(keysOf(IDENTITY_A), MembershipContract.Issue)
                failsWith(MembershipContract.Issue.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On membership issuance, only one state must be created (centralized)`() {
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
    fun `On membership issuance, only one state must be created (decentralized)`() {
        services.ledger {
            transaction {
                output(MembershipContract.ID, DECENTRALIZED_MEMBERSHIP_A)
                output(MembershipContract.ID, DECENTRALIZED_MEMBERSHIP_A)
                command(keysOf(IDENTITY_A), MembershipContract.Issue)
                failsWith(MembershipContract.Issue.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On membership issuance, network operators must possess the network operator role (centralized)`() {
        services.ledger {
            transaction {
                output(MembershipContract.ID, CENTRALIZED_MEMBERSHIP_O.copy(roles = emptySet()))
                command(keysOf(OPERATOR_A), MembershipContract.Issue)
                failsWith(MembershipContract.Issue.CONTRACT_RULE_ROLES)
            }
        }
    }

    @Test
    fun `On membership issuance, the previous state reference must be null (centralized)`() {
        services.ledger {
            transaction {
                output(MembershipContract.ID, CENTRALIZED_MEMBERSHIP_A.copy(previousStateRef = INVALID_STATEREF))
                command(keysOf(OPERATOR_A), MembershipContract.Issue)
                failsWith(MembershipContract.Issue.CONTRACT_RULE_PREVIOUS_REF)
            }
        }
    }

    @Test
    fun `On membership issuance, the previous state reference must be null (decentralized)`() {
        services.ledger {
            transaction {
                output(MembershipContract.ID, DECENTRALIZED_MEMBERSHIP_A.copy(previousStateRef = INVALID_STATEREF))
                command(keysOf(IDENTITY_A), MembershipContract.Issue)
                failsWith(MembershipContract.Issue.CONTRACT_RULE_PREVIOUS_REF)
            }
        }
    }

    @Test
    fun `On membership issuance, only the network member must sign the transaction (centralized)`() {
        services.ledger {
            transaction {
                output(MembershipContract.ID, CENTRALIZED_MEMBERSHIP_A)
                command(keysOf(IDENTITY_B), MembershipContract.Issue)
                failsWith(MembershipContract.Issue.CONTRACT_RULE_SIGNERS)
            }
        }
    }

    @Test
    fun `On membership issuance, only the network member must sign the transaction (decentralized)`() {
        services.ledger {
            transaction {
                output(MembershipContract.ID, DECENTRALIZED_MEMBERSHIP_A)
                command(keysOf(IDENTITY_B), MembershipContract.Issue)
                failsWith(MembershipContract.Issue.CONTRACT_RULE_SIGNERS)
            }
        }
    }
}