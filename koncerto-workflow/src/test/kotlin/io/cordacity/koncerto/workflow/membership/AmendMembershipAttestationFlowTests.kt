package io.cordacity.koncerto.workflow.membership

import io.cordacity.koncerto.contract.AttestationStatus
import io.cordacity.koncerto.contract.membership.MembershipAttestationState
import io.cordacity.koncerto.contract.membership.MembershipState
import io.cordacity.koncerto.workflow.DummyIdentity
import io.cordacity.koncerto.workflow.MockNetworkFlowTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.test.assertEquals

class AmendMembershipAttestationFlowTests : MockNetworkFlowTest() {

    @Test
    fun `AmendMembershipAttestationFlow transaction should be signed by the initiator (centralized)`() {

        // Act
        val membership = alice.issueMembership(CENTRALIZED_NETWORK, setOf(operator.party)).first
            .tx.outRefsOfType<MembershipState<DummyIdentity>>().single()
        val oldAttestation = operator.issueMembershipAttestation(membership).first
            .tx.outRefsOfType<MembershipAttestationState>().single()
        val newAttestation = oldAttestation.state.data.accept()
        val (transaction) = operator.amendMembershipAttestation(oldAttestation, newAttestation)

        // Assert
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `AmendMembershipAttestationFlow transaction should be signed by the initiator (decentralized)`() {

        // Act
        val membership = alice.issueMembership(DECENTRALIZED_NETWORK, setOf(bob.party, charlie.party)).first
            .tx.outRefsOfType<MembershipState<DummyIdentity>>().single()
        val oldAttestation = bob.issueMembershipAttestation(membership).first
            .tx.outRefsOfType<MembershipAttestationState>().single()
        val newAttestation = oldAttestation.state.data.accept()
        val (transaction) = bob.amendMembershipAttestation(oldAttestation, newAttestation)

        // Assert
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `AmendMembershipAttestationFlow should record a transaction for all participants (centralized)`() {

        // Act
        val membership = alice.issueMembership(CENTRALIZED_NETWORK, setOf(operator.party)).first
            .tx.outRefsOfType<MembershipState<DummyIdentity>>().single()
        val oldAttestation = operator.issueMembershipAttestation(membership).first
            .tx.outRefsOfType<MembershipAttestationState>().single()
        val newAttestation = oldAttestation.state.data.accept()
        val (transaction, attestation) = operator.amendMembershipAttestation(oldAttestation, newAttestation)

        // Assert
        listOf(alice, operator).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Could not find a recorded transaction with id: ${transaction.id}.")

                assertEquals(transaction, recordedTransaction)
                assertEquals(1, recordedTransaction.tx.inputs.size)
                assertEquals(1, recordedTransaction.tx.references.size)
                assertEquals(1, recordedTransaction.tx.outputs.size)

                val recordedAttestation = recordedTransaction
                    .tx.outputsOfType<MembershipAttestationState>().single()

                assertEquals(attestation, recordedAttestation)
                assertEquals(AttestationStatus.ACCEPTED, recordedAttestation.status)
            }
        }
    }

    @Test
    fun `AmendMembershipAttestationFlow should record a transaction for all participants (decentralized)`() {

        // Act
        val membership = alice.issueMembership(DECENTRALIZED_NETWORK, setOf(bob.party, charlie.party)).first
            .tx.outRefsOfType<MembershipState<DummyIdentity>>().single()
        val oldAttestation = bob.issueMembershipAttestation(membership).first
            .tx.outRefsOfType<MembershipAttestationState>().single()
        val newAttestation = oldAttestation.state.data.accept()
        val (transaction, attestation) = bob.amendMembershipAttestation(oldAttestation, newAttestation)

        // Assert
        listOf(alice, bob).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Could not find a recorded transaction with id: ${transaction.id}.")

                assertEquals(transaction, recordedTransaction)
                assertEquals(1, recordedTransaction.tx.inputs.size)
                assertEquals(1, recordedTransaction.tx.references.size)
                assertEquals(1, recordedTransaction.tx.outputs.size)

                val recordedAttestation = recordedTransaction
                    .tx.outputsOfType<MembershipAttestationState>().single()

                assertEquals(attestation, recordedAttestation)
                assertEquals(AttestationStatus.ACCEPTED, recordedAttestation.status)
            }
        }
    }
}