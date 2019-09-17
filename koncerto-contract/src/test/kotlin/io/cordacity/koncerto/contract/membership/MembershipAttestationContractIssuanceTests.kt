package io.cordacity.koncerto.contract.membership

import io.cordacity.koncerto.contract.*
import io.cordacity.koncerto.contract.membership.MembershipAttestationContract
import io.cordacity.koncerto.contract.membership.MembershipContract
import net.corda.core.contracts.UniqueIdentifier
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class MembershipAttestationContractIssuanceTests : ContractTest() {

    @Test
    fun `On membership attestation issuance, the transaction must include the Issue command (centralized)`() {
        services.ledger {
            transaction {
                val (membership, attestation) = initialize(CENTRALIZED_MEMBERSHIP_A, OPERATOR_A.party)
                output(MembershipAttestationContract.ID, attestation)
                reference(membership.ref)
                fails()
                command(keysOf(OPERATOR_A), MembershipAttestationContract.Issue)
                verifies()
            }
        }
    }

    @Test
    fun `On membership attestation issuance, the transaction must include the Issue command (decentralized)`() {
        services.ledger {
            transaction {
                val (membership, attestation) = initialize(DECENTRALIZED_MEMBERSHIP_A, IDENTITY_B.party)
                output(MembershipAttestationContract.ID, attestation)
                reference(membership.ref)
                fails()
                command(keysOf(IDENTITY_B), MembershipAttestationContract.Issue)
                verifies()
            }
        }
    }

    @Test
    fun `On membership attestation issuance, zero states must be consumed (centralized)`() {
        services.ledger {
            transaction {
                val (membership, attestation) = initialize(CENTRALIZED_MEMBERSHIP_A, OPERATOR_A.party)
                input(MembershipAttestationContract.ID, attestation)
                output(MembershipAttestationContract.ID, attestation)
                reference(membership.ref)
                command(keysOf(OPERATOR_A), MembershipAttestationContract.Issue)
                failsWith(MembershipAttestationContract.Issue.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On membership attestation issuance, zero states must be consumed (decentralized)`() {
        services.ledger {
            transaction {
                val (membership, attestation) = initialize(DECENTRALIZED_MEMBERSHIP_A, IDENTITY_B.party)
                input(MembershipAttestationContract.ID, attestation)
                output(MembershipAttestationContract.ID, attestation)
                reference(membership.ref)
                command(keysOf(IDENTITY_B), MembershipAttestationContract.Issue)
                failsWith(MembershipAttestationContract.Issue.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On membership attestation issuance, only one state must be created (centralized)`() {
        services.ledger {
            transaction {
                val (membership, attestation) = initialize(CENTRALIZED_MEMBERSHIP_A, OPERATOR_A.party)
                output(MembershipAttestationContract.ID, attestation)
                output(MembershipAttestationContract.ID, attestation)
                reference(membership.ref)
                command(keysOf(OPERATOR_A), MembershipAttestationContract.Issue)
                failsWith(MembershipAttestationContract.Issue.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On membership attestation issuance, only one state must be created (decentralized)`() {
        services.ledger {
            transaction {
                val (membership, attestation) = initialize(DECENTRALIZED_MEMBERSHIP_A, IDENTITY_B.party)
                output(MembershipAttestationContract.ID, attestation)
                output(MembershipAttestationContract.ID, attestation)
                reference(membership.ref)
                command(keysOf(IDENTITY_B), MembershipAttestationContract.Issue)
                failsWith(MembershipAttestationContract.Issue.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On membership attestation issuance, only one membership state must be referenced (centralized)`() {
        services.ledger {
            transaction {
                val (membership, attestation) = initialize(CENTRALIZED_MEMBERSHIP_A, IDENTITY_B.party)
                output(MembershipAttestationContract.ID, attestation)
                reference(MembershipContract.ID, membership.state.data)
                reference(membership.ref)
                command(keysOf(IDENTITY_B), MembershipAttestationContract.Issue)
                failsWith(MembershipAttestationContract.Issue.CONTRACT_RULE_REFERENCES)
            }
        }
    }

    @Test
    fun `On membership attestation issuance, only one membership state must be referenced (decentralized)`() {
        services.ledger {
            transaction {
                val (membership, attestation) = initialize(DECENTRALIZED_MEMBERSHIP_A, IDENTITY_B.party)
                output(MembershipAttestationContract.ID, attestation)
                reference(MembershipContract.ID, membership.state.data)
                reference(membership.ref)
                command(keysOf(IDENTITY_B), MembershipAttestationContract.Issue)
                failsWith(MembershipAttestationContract.Issue.CONTRACT_RULE_REFERENCES)
            }
        }
    }

    @Test
    fun `On membership attestation issuance, the attestation pointer must point to the referenced membership state (centralized)`() {
        services.ledger {
            transaction {
                val (membership, attestation) = initialize(CENTRALIZED_MEMBERSHIP_A, OPERATOR_A.party)
                val oldPointer = attestation.pointer
                val newPointer = AttestationPointer(UniqueIdentifier(), oldPointer.stateRef, oldPointer.type)
                output(MembershipAttestationContract.ID, attestation.copy(pointer = newPointer))
                reference(membership.ref)
                command(keysOf(OPERATOR_A), MembershipAttestationContract.Issue)
                failsWith(MembershipAttestationContract.Issue.CONTRACT_RULE_POINTER)
            }
        }
    }

    @Test
    fun `On membership attestation issuance, the attestation pointer must point to the referenced membership state (decentralized)`() {
        services.ledger {
            transaction {
                val (membership, attestation) = initialize(DECENTRALIZED_MEMBERSHIP_A, IDENTITY_B.party)
                val oldPointer = attestation.pointer
                val newPointer = AttestationPointer(UniqueIdentifier(), oldPointer.stateRef, oldPointer.type)
                output(MembershipAttestationContract.ID, attestation.copy(pointer = newPointer))
                reference(membership.ref)
                command(keysOf(IDENTITY_B), MembershipAttestationContract.Issue)
                failsWith(MembershipAttestationContract.Issue.CONTRACT_RULE_POINTER)
            }
        }
    }

    @Test
    fun `On membership attestation issuance, the attestee must be the network identity of the referenced membership state (centralized)`() {
        services.ledger {
            transaction {
                val (membership, attestation) = initialize(CENTRALIZED_MEMBERSHIP_A, OPERATOR_A.party)
                output(MembershipAttestationContract.ID, attestation.copy(attestees = setOf(IDENTITY_C.party)))
                reference(membership.ref)
                command(keysOf(OPERATOR_A), MembershipAttestationContract.Issue)
                failsWith(MembershipAttestationContract.Issue.CONTRACT_RULE_MEMBERSHIP)
            }
        }
    }

    @Test
    fun `On membership attestation issuance, the attestee must be the network identity of the referenced membership state (decentralized)`() {
        services.ledger {
            transaction {
                val (membership, attestation) = initialize(DECENTRALIZED_MEMBERSHIP_A, IDENTITY_B.party)
                output(MembershipAttestationContract.ID, attestation.copy(attestees = setOf(IDENTITY_C.party)))
                reference(membership.ref)
                command(keysOf(IDENTITY_B), MembershipAttestationContract.Issue)
                failsWith(MembershipAttestationContract.Issue.CONTRACT_RULE_MEMBERSHIP)
            }
        }
    }

    @Test
    fun `On membership attestation amendment, if present, only the network operator must attest membership (centralized)`() {
        services.ledger {
            transaction {
                val (membership, attestation) = initialize(CENTRALIZED_MEMBERSHIP_A, IDENTITY_B.party)
                output(MembershipAttestationContract.ID, attestation)
                reference(membership.ref)
                command(keysOf(IDENTITY_B), MembershipAttestationContract.Issue)
                failsWith(MembershipAttestationContract.Issue.CONTRACT_RULE_OPERATOR)
            }
        }
    }

    @Test
    fun `On membership attestation issuance, the attestation and membership network hash must be equal (centralized)`() {
        services.ledger {
            transaction {
                val (membership, attestation) = initialize(CENTRALIZED_MEMBERSHIP_A, OPERATOR_A.party)
                output(MembershipAttestationContract.ID, attestation.copy(network = INVALID_NETWORK))
                reference(membership.ref)
                command(keysOf(OPERATOR_A), MembershipAttestationContract.Issue)
                failsWith(MembershipAttestationContract.Issue.CONTRACT_RULE_NETWORK_HASH)
            }
        }
    }

    @Test
    fun `On membership attestation issuance, the attestation and membership network hash must be equal (decentralized)`() {
        services.ledger {
            transaction {
                val (membership, attestation) = initialize(DECENTRALIZED_MEMBERSHIP_A, IDENTITY_B.party)
                output(MembershipAttestationContract.ID, attestation.copy(network = INVALID_NETWORK))
                reference(membership.ref)
                command(keysOf(IDENTITY_B), MembershipAttestationContract.Issue)
                failsWith(MembershipAttestationContract.Issue.CONTRACT_RULE_NETWORK_HASH)
            }
        }
    }

    @Test
    fun `On membership attestation issuance, only the attestor must sign the transaction (centralized)`() {
        services.ledger {
            transaction {
                val (membership, attestation) = initialize(CENTRALIZED_MEMBERSHIP_A, OPERATOR_A.party)
                output(MembershipAttestationContract.ID, attestation)
                reference(membership.ref)
                command(keysOf(IDENTITY_B), MembershipAttestationContract.Issue)
                failsWith(MembershipAttestationContract.Issue.CONTRACT_RULE_SIGNERS)
            }
        }
    }

    @Test
    fun `On membership attestation issuance, only the attestor must sign the transaction (decentralized)`() {
        services.ledger {
            transaction {
                val (membership, attestation) = initialize(DECENTRALIZED_MEMBERSHIP_A, IDENTITY_B.party)
                output(MembershipAttestationContract.ID, attestation)
                reference(membership.ref)
                command(keysOf(IDENTITY_C), MembershipAttestationContract.Issue)
                failsWith(MembershipAttestationContract.Issue.CONTRACT_RULE_SIGNERS)
            }
        }
    }
}