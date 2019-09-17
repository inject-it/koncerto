package io.cordacity.koncerto.contract.membership

import io.cordacity.koncerto.contract.*
import io.cordacity.koncerto.contract.membership.MembershipAttestationContract
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class MembershipAttestationContractRevocationTests : ContractTest() {

    @Test
    fun `On membership attestation revocation, the transaction must include the Revoke command (centralized)`() {
        services.ledger {
            transaction {
                val (_, attestation) = initialize(CENTRALIZED_MEMBERSHIP_A, OPERATOR_A.party)
                input(MembershipAttestationContract.ID, attestation)
                fails()
                command(keysOf(OPERATOR_A), MembershipAttestationContract.Revoke)
                verifies()
            }
        }
    }

    @Test
    fun `On membership attestation revocation, the transaction must include the Revoke command (decentralized)`() {
        services.ledger {
            transaction {
                val (_, attestation) = initialize(DECENTRALIZED_MEMBERSHIP_A, IDENTITY_B.party)
                input(MembershipAttestationContract.ID, attestation)
                fails()
                command(keysOf(IDENTITY_B), MembershipAttestationContract.Revoke)
                verifies()
            }
        }
    }

    @Test
    fun `On membership attestation revocation, zero states must be consumed (centralized)`() {
        services.ledger {
            transaction {
                val (_, attestation) = initialize(CENTRALIZED_MEMBERSHIP_A, OPERATOR_A.party)
                input(MembershipAttestationContract.ID, attestation)
                input(MembershipAttestationContract.ID, attestation)
                command(keysOf(OPERATOR_A), MembershipAttestationContract.Revoke)
                failsWith(MembershipAttestationContract.Revoke.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On membership attestation revocation, zero states must be consumed (decentralized)`() {
        services.ledger {
            transaction {
                val (_, attestation) = initialize(DECENTRALIZED_MEMBERSHIP_A, IDENTITY_B.party)
                input(MembershipAttestationContract.ID, attestation)
                input(MembershipAttestationContract.ID, attestation)
                command(keysOf(IDENTITY_B), MembershipAttestationContract.Revoke)
                failsWith(MembershipAttestationContract.Revoke.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On membership attestation revocation, only one state must be created (centralized)`() {
        services.ledger {
            transaction {
                val (_, attestation) = initialize(CENTRALIZED_MEMBERSHIP_A, OPERATOR_A.party)
                input(MembershipAttestationContract.ID, attestation)
                output(MembershipAttestationContract.ID, attestation)
                command(keysOf(OPERATOR_A), MembershipAttestationContract.Revoke)
                failsWith(MembershipAttestationContract.Revoke.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On membership attestation revocation, only one state must be created (decentralized)`() {
        services.ledger {
            transaction {
                val (_, attestation) = initialize(DECENTRALIZED_MEMBERSHIP_A, IDENTITY_B.party)
                input(MembershipAttestationContract.ID, attestation)
                output(MembershipAttestationContract.ID, attestation)
                command(keysOf(IDENTITY_B), MembershipAttestationContract.Revoke)
                failsWith(MembershipAttestationContract.Revoke.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On membership attestation revocation, only the attestor must sign the transaction (centralized)`() {
        services.ledger {
            transaction {
                val (_, attestation) = initialize(CENTRALIZED_MEMBERSHIP_A, OPERATOR_A.party)
                input(MembershipAttestationContract.ID, attestation)
                command(keysOf(IDENTITY_B), MembershipAttestationContract.Revoke)
                failsWith(MembershipAttestationContract.Revoke.CONTRACT_RULE_SIGNERS)
            }
        }
    }

    @Test
    fun `On membership attestation revocation, only the attestor must sign the transaction (decentralized)`() {
        services.ledger {
            transaction {
                val (_, attestation) = initialize(DECENTRALIZED_MEMBERSHIP_A, IDENTITY_B.party)
                input(MembershipAttestationContract.ID, attestation)
                command(keysOf(IDENTITY_C), MembershipAttestationContract.Revoke)
                failsWith(MembershipAttestationContract.Revoke.CONTRACT_RULE_SIGNERS)
            }
        }
    }
}