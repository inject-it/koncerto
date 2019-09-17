package io.cordacity.koncerto.workflow.membership

import io.cordacity.koncerto.contract.membership.MembershipAttestationState
import io.cordacity.koncerto.contract.membership.MembershipState
import io.cordacity.koncerto.workflow.DummyIdentity
import io.cordacity.koncerto.workflow.MockNetworkFlowTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.test.assertEquals

class RevokeMembershipAttestationFlowTests : MockNetworkFlowTest() {

    @Test
    fun `RevokeMembershipAttestationFlow transaction should be signed by the initiator (centralized)`() {

        // Act
        val membership = alice.issueMembership(CENTRALIZED_NETWORK, setOf(operator.party)).first
            .tx.outRefsOfType<MembershipState<DummyIdentity>>().single()
        val attestation = operator.issueMembershipAttestation(membership).first
            .tx.outRefsOfType<MembershipAttestationState>().single()
        val transaction = operator.revokeMembershipAttestation(attestation)

        // Assert
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `RevokeMembershipAttestationFlow transaction should be signed by the initiator (decentralized)`() {

        // Act
        val membership = alice.issueMembership(DECENTRALIZED_NETWORK, setOf(bob.party)).first
            .tx.outRefsOfType<MembershipState<DummyIdentity>>().single()
        val attestation = bob.issueMembershipAttestation(membership).first
            .tx.outRefsOfType<MembershipAttestationState>().single()
        val transaction = bob.revokeMembershipAttestation(attestation)

        // Assert
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `RevokeMembershipAttestationFlow should record a transaction for all participants (centralized)`() {

        // Act
        val membership = alice.issueMembership(CENTRALIZED_NETWORK, setOf(operator.party)).first
            .tx.outRefsOfType<MembershipState<DummyIdentity>>().single()
        val attestation = operator.issueMembershipAttestation(membership).first
            .tx.outRefsOfType<MembershipAttestationState>().single()
        val transaction = operator.revokeMembershipAttestation(attestation)

        // Assert
        listOf(alice, operator).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Could not find a recorded transaction with id: ${transaction.id}.")

                assertEquals(transaction, recordedTransaction)
                assertEquals(1, recordedTransaction.tx.inputs.size)
            }
        }
    }

    @Test
    fun `RevokeMembershipAttestationFlow should record a transaction for all participants (decentralized)`() {

        // Act
        val membership = alice.issueMembership(DECENTRALIZED_NETWORK, setOf(bob.party, charlie.party)).first
            .tx.outRefsOfType<MembershipState<DummyIdentity>>().single()
        val attestation = bob.issueMembershipAttestation(membership).first
            .tx.outRefsOfType<MembershipAttestationState>().single()
        val transaction = bob.revokeMembershipAttestation(attestation)

        // Assert
        listOf(alice, bob).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Could not find a recorded transaction with id: ${transaction.id}.")

                assertEquals(transaction, recordedTransaction)
                assertEquals(1, recordedTransaction.tx.inputs.size)
            }
        }
    }
}