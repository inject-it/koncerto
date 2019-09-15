package io.cordacity.koncerto.contract.membership.decentralized

import io.cordacity.koncerto.contract.ContractTest
import io.cordacity.koncerto.contract.DECENTRALIZED_MEMBERSHIP_A
import io.cordacity.koncerto.contract.IDENTITY_B
import io.cordacity.koncerto.contract.IDENTITY_C
import io.cordacity.koncerto.contract.membership.MembershipAttestationContract
import net.corda.testing.node.ledger
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Decentralized membership attestation contract revocation tests")
class MembershipAttestationContractRevocationTests : ContractTest() {

    @Test
    fun `On membership attestation revocation, the transaction must include the Revoke command`() {
        services.ledger {
            transaction {
                val (_, attestation) = initialize(this@ledger, DECENTRALIZED_MEMBERSHIP_A, IDENTITY_B.party)
                input(MembershipAttestationContract.ID, attestation)
                fails()
                command(keysOf(IDENTITY_B), MembershipAttestationContract.Revoke)
                verifies()
            }
        }
    }

    @Test
    fun `On membership attestation revocation, zero states must be consumed`() {
        services.ledger {
            transaction {
                val (_, attestation) = initialize(this@ledger, DECENTRALIZED_MEMBERSHIP_A, IDENTITY_B.party)
                input(MembershipAttestationContract.ID, attestation)
                input(MembershipAttestationContract.ID, attestation)
                command(keysOf(IDENTITY_B), MembershipAttestationContract.Revoke)
                failsWith(MembershipAttestationContract.Revoke.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On membership attestation revocation, only one state must be created`() {
        services.ledger {
            transaction {
                val (_, attestation) = initialize(this@ledger, DECENTRALIZED_MEMBERSHIP_A, IDENTITY_B.party)
                input(MembershipAttestationContract.ID, attestation)
                output(MembershipAttestationContract.ID, attestation)
                command(keysOf(IDENTITY_B), MembershipAttestationContract.Revoke)
                failsWith(MembershipAttestationContract.Revoke.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On membership attestation revocation, only the attestor must sign the transaction`() {
        services.ledger {
            transaction {
                val (_, attestation) = initialize(this@ledger, DECENTRALIZED_MEMBERSHIP_A, IDENTITY_B.party)
                input(MembershipAttestationContract.ID, attestation)
                command(keysOf(IDENTITY_C), MembershipAttestationContract.Revoke)
                failsWith(MembershipAttestationContract.Revoke.CONTRACT_RULE_SIGNERS)
            }
        }
    }
}