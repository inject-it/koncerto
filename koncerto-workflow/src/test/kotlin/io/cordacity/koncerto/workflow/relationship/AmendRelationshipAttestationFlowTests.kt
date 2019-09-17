package io.cordacity.koncerto.workflow.relationship

import io.cordacity.koncerto.contract.AttestationStatus
import io.cordacity.koncerto.contract.relationship.RelationshipAttestationState
import io.cordacity.koncerto.contract.relationship.RelationshipState
import io.cordacity.koncerto.workflow.DummyConfig
import io.cordacity.koncerto.workflow.MockNetworkFlowTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.test.assertEquals

class AmendRelationshipAttestationFlowTests : MockNetworkFlowTest() {

    @Test
    fun `AmendRelationshipAttestationFlow transaction should be signed by all participants (issued by member, centralized)`() {

        // Act
        val relationship = alice.issueRelationship(CENTRALIZED_RELATIONSHIP)
            .first.tx.outRefsOfType<RelationshipState<DummyConfig>>().single()
        val oldAttestation = alice.issueRelationshipAttestation(relationship)
            .first.tx.outRefsOfType<RelationshipAttestationState>().single()
        val newAttestation = oldAttestation.state.data.accept()
        val (transaction) = alice.amendRelationshipAttestation(oldAttestation, newAttestation)

        // Assert
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `AmendRelationshipAttestationFlow transaction should be signed by all participants (issued by operator, centralized)`() {

        // Act
        val relationship = alice.issueRelationship(CENTRALIZED_RELATIONSHIP)
            .first.tx.outRefsOfType<RelationshipState<DummyConfig>>().single()
        val oldAttestation = operator.issueRelationshipAttestation(relationship)
            .first.tx.outRefsOfType<RelationshipAttestationState>().single()
        val newAttestation = oldAttestation.state.data.accept()
        val (transaction) = operator.amendRelationshipAttestation(oldAttestation, newAttestation)

        // Assert
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `AmendRelationshipAttestationFlow transaction should be signed by all participants (issued by member, decentralized)`() {

        // Act
        val relationship = alice.issueRelationship(DECENTRALIZED_RELATIONSHIP)
            .first.tx.outRefsOfType<RelationshipState<DummyConfig>>().single()
        val oldAttestation = alice.issueRelationshipAttestation(relationship)
            .first.tx.outRefsOfType<RelationshipAttestationState>().single()
        val newAttestation = oldAttestation.state.data.accept()
        val (transaction) = alice.amendRelationshipAttestation(oldAttestation, newAttestation)

        // Assert
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `AmendRelationshipAttestationFlow should record a transaction for all participants (centralized)`() {

        // Act
        val relationship = alice.issueRelationship(CENTRALIZED_RELATIONSHIP)
            .first.tx.outRefsOfType<RelationshipState<DummyConfig>>().single()
        val oldAttestation = alice.issueRelationshipAttestation(relationship)
            .first.tx.outRefsOfType<RelationshipAttestationState>().single()
        val newAttestation = oldAttestation.state.data.accept()
        val (transaction) = alice.amendRelationshipAttestation(oldAttestation, newAttestation)

        // Assert
        listOf(alice, bob, charlie, operator).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Could not find a recorded transaction with id: ${transaction.id}.")

                assertEquals(transaction, recordedTransaction)
                assertEquals(1, recordedTransaction.tx.inputs.size)
                assertEquals(1, recordedTransaction.tx.outputs.size)

                val recordedAttestation = recordedTransaction
                    .tx.outputsOfType<RelationshipAttestationState>().single()

                assertEquals(newAttestation, recordedAttestation)
                assertEquals(AttestationStatus.ACCEPTED, newAttestation.status)
            }
        }
    }

    @Test
    fun `AmendRelationshipAttestationFlow should record a transaction for all participants (decentralized)`() {

        // Act
        val relationship = alice.issueRelationship(CENTRALIZED_RELATIONSHIP)
            .first.tx.outRefsOfType<RelationshipState<DummyConfig>>().single()
        val oldAttestation = alice.issueRelationshipAttestation(relationship)
            .first.tx.outRefsOfType<RelationshipAttestationState>().single()
        val newAttestation = oldAttestation.state.data.accept()
        val (transaction) = alice.amendRelationshipAttestation(oldAttestation, newAttestation)

        // Assert
        listOf(alice, bob, charlie).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Could not find a recorded transaction with id: ${transaction.id}.")

                assertEquals(transaction, recordedTransaction)
                assertEquals(1, recordedTransaction.tx.inputs.size)
                assertEquals(1, recordedTransaction.tx.outputs.size)

                val recordedAttestation = recordedTransaction
                    .tx.outputsOfType<RelationshipAttestationState>().single()

                assertEquals(newAttestation, recordedAttestation)
                assertEquals(AttestationStatus.ACCEPTED, newAttestation.status)
            }
        }
    }
}