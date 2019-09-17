package io.cordacity.koncerto.workflow.membership

import io.cordacity.koncerto.contract.membership.MembershipAttestationState
import io.cordacity.koncerto.contract.membership.MembershipState
import io.cordacity.koncerto.workflow.DummyIdentity
import io.cordacity.koncerto.workflow.MockNetworkFlowTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.test.assertEquals

class IssueMembershipAttestationFlowTests : MockNetworkFlowTest() {

    @Test
    fun `IssueMembershipAttestationFlow transaction should be signed by the initiator (centralized)`() {

        // Act
        val membership = alice.issueMembership(CENTRALIZED_NETWORK, setOf(operator.party)).first
            .tx.outRefsOfType<MembershipState<DummyIdentity>>().single()
        val (transaction) = operator.issueMembershipAttestation(membership)

        // Assert
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `IssueMembershipAttestationFlow transaction should be signed by the initiator (decentralized)`() {

        // Act
        val membership = alice.issueMembership(DECENTRALIZED_NETWORK, setOf(bob.party, charlie.party)).first
            .tx.outRefsOfType<MembershipState<DummyIdentity>>().single()
        val (transaction) = bob.issueMembershipAttestation(membership)

        // Assert
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `IssueMembershipAttestationFlow should record a transaction for all participants (centralized)`() {

        // Act
        val membership = alice.issueMembership(CENTRALIZED_NETWORK, setOf(operator.party)).first
            .tx.outRefsOfType<MembershipState<DummyIdentity>>().single()
        val (transaction, attestation) = operator.issueMembershipAttestation(membership)

        // Assert
        listOf(alice, operator).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Could not find a recorded transaction with id: ${transaction.id}.")

                assertEquals(transaction, recordedTransaction)
                assertEquals(0, recordedTransaction.tx.inputs.size)
                assertEquals(1, recordedTransaction.tx.references.size)
                assertEquals(1, recordedTransaction.tx.outputs.size)

                val recordedAttestation = recordedTransaction
                    .tx.outputsOfType<MembershipAttestationState>().single()

                assertEquals(attestation, recordedAttestation)
            }
        }
    }

    @Test
    fun `IssueMembershipAttestationFlow should record a transaction for all participants (decentralized)`() {

        // Act
        val membership = alice.issueMembership(DECENTRALIZED_NETWORK, setOf(bob.party, charlie.party)).first
            .tx.outRefsOfType<MembershipState<DummyIdentity>>().single()
        val (transaction, attestation) = bob.issueMembershipAttestation(membership)

        // Assert
        listOf(alice, bob).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Could not find a recorded transaction with id: ${transaction.id}.")

                assertEquals(transaction, recordedTransaction)
                assertEquals(0, recordedTransaction.tx.inputs.size)
                assertEquals(1, recordedTransaction.tx.references.size)
                assertEquals(1, recordedTransaction.tx.outputs.size)

                val recordedAttestation = recordedTransaction
                    .tx.outputsOfType<MembershipAttestationState>().single()

                assertEquals(attestation, recordedAttestation)
            }
        }
    }
}