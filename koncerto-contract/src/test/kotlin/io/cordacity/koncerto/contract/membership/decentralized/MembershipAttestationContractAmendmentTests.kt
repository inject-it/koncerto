package io.cordacity.koncerto.contract.membership.decentralized

import io.cordacity.koncerto.contract.*
import io.cordacity.koncerto.contract.membership.MembershipAttestationContract
import io.cordacity.koncerto.contract.membership.MembershipContract
import net.corda.core.contracts.UniqueIdentifier
import net.corda.testing.node.ledger
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Decentralized membership attestation contract issuance tests")
class MembershipAttestationContractAmendmentTests : ContractTest() {

    @Test
    fun `On membership attestation amendment, the transaction must include the Amend command`() {
        services.ledger {
            transaction {
                val (membership, attestation) = initialize(DECENTRALIZED_MEMBERSHIP_A, IDENTITY_B.party)
                input(MembershipAttestationContract.ID, attestation)
                output(MembershipAttestationContract.ID, attestation)
                reference(membership.ref)
                fails()
                command(keysOf(IDENTITY_B), MembershipAttestationContract.Amend)
                verifies()
            }
        }
    }

    @Test
    fun `On membership attestation amendment, only one state must be consumed`() {
        services.ledger {
            transaction {
                val (membership, attestation) = initialize(DECENTRALIZED_MEMBERSHIP_A, IDENTITY_B.party)
                input(MembershipAttestationContract.ID, attestation)
                input(MembershipAttestationContract.ID, attestation)
                output(MembershipAttestationContract.ID, attestation)
                reference(membership.ref)
                command(keysOf(IDENTITY_B), MembershipAttestationContract.Amend)
                failsWith(MembershipAttestationContract.Amend.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On membership attestation amendment, only one state must be created`() {
        services.ledger {
            transaction {
                val (membership, attestation) = initialize(DECENTRALIZED_MEMBERSHIP_A, IDENTITY_B.party)
                input(MembershipAttestationContract.ID, attestation)
                output(MembershipAttestationContract.ID, attestation)
                output(MembershipAttestationContract.ID, attestation)
                reference(membership.ref)
                command(keysOf(IDENTITY_B), MembershipAttestationContract.Amend)
                failsWith(MembershipAttestationContract.Amend.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On membership attestation amendment, only one membership state must be referenced`() {
        services.ledger {
            transaction {
                val (membership, attestation) = initialize(DECENTRALIZED_MEMBERSHIP_A, IDENTITY_B.party)
                input(MembershipAttestationContract.ID, attestation)
                output(MembershipAttestationContract.ID, attestation)
                reference(MembershipContract.ID, membership.state.data)
                reference(membership.ref)
                command(keysOf(IDENTITY_B), MembershipAttestationContract.Amend)
                failsWith(MembershipAttestationContract.Amend.CONTRACT_RULE_REFERENCES)
            }
        }
    }

    @Test
    fun `On membership attestation amendment, the attestation pointer must point to the referenced membership state`() {
        services.ledger {
            transaction {
                val (membership, attestation) = initialize(DECENTRALIZED_MEMBERSHIP_A, IDENTITY_B.party)
                val oldPointer = attestation.pointer
                val newPointer = AttestationPointer(UniqueIdentifier(), oldPointer.stateRef, oldPointer.type)
                input(MembershipAttestationContract.ID, attestation)
                output(MembershipAttestationContract.ID, attestation.copy(pointer = newPointer))
                reference(membership.ref)
                command(keysOf(IDENTITY_B), MembershipAttestationContract.Amend)
                failsWith(MembershipAttestationContract.Amend.CONTRACT_RULE_POINTER)
            }
        }
    }

    @Test
    fun `On membership attestation amendment, the attestee must be the network identity of the referenced membership state`() {
        services.ledger {
            transaction {
                val (membership, attestation) = initialize(DECENTRALIZED_MEMBERSHIP_A, IDENTITY_B.party)
                input(MembershipAttestationContract.ID, attestation)
                output(MembershipAttestationContract.ID, attestation.copy(attestees = setOf(IDENTITY_C.party)))
                reference(membership.ref)
                command(keysOf(IDENTITY_B), MembershipAttestationContract.Amend)
                failsWith(MembershipAttestationContract.Amend.CONTRACT_RULE_MEMBERSHIP)
            }
        }
    }

    @Test
    fun `On membership attestation amendment, the attestation and membership network hash must be equal`() {
        services.ledger {
            transaction {
                val (membership, attestation) = initialize(DECENTRALIZED_MEMBERSHIP_A, IDENTITY_B.party)
                input(MembershipAttestationContract.ID, attestation)
                output(MembershipAttestationContract.ID, attestation.copy(network = INVALID_NETWORK))
                reference(membership.ref)
                command(keysOf(IDENTITY_B), MembershipAttestationContract.Amend)
                failsWith(MembershipAttestationContract.Amend.CONTRACT_RULE_NETWORK_HASH)
            }
        }
    }

    @Test
    fun `On membership attestation amendment, only the attestor must sign the transaction`() {
        services.ledger {
            transaction {
                val (membership, attestation) = initialize(DECENTRALIZED_MEMBERSHIP_A, IDENTITY_B.party)
                input(MembershipAttestationContract.ID, attestation)
                output(MembershipAttestationContract.ID, attestation)
                reference(membership.ref)
                command(keysOf(IDENTITY_C), MembershipAttestationContract.Amend)
                failsWith(MembershipAttestationContract.Amend.CONTRACT_RULE_SIGNERS)
            }
        }
    }
}