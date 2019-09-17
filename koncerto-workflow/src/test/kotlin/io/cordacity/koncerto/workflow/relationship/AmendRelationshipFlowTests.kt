package io.cordacity.koncerto.workflow.relationship

import io.cordacity.koncerto.contract.getNextOutput
import io.cordacity.koncerto.contract.relationship.RelationshipState
import io.cordacity.koncerto.workflow.DummyConfig
import io.cordacity.koncerto.workflow.MockNetworkFlowTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.test.assertEquals

class AmendRelationshipFlowTests : MockNetworkFlowTest() {

    @Test
    fun `AmendRelationshipFlow transaction should be signed by all participants (initiated by alice, centralized)`() {

        // Act
        val oldRelationship = alice.issueRelationship(CENTRALIZED_RELATIONSHIP)
            .first.tx.outRefsOfType<RelationshipState<DummyConfig>>().single()
        val newRelationship = oldRelationship.getNextOutput()
        val (transaction) = alice.amendRelationship(oldRelationship, newRelationship)

        // Assert
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `AmendRelationshipFlow transaction should be signed by all participants (initiated by bob, centralized)`() {

        // Act
        val oldRelationship = alice.issueRelationship(CENTRALIZED_RELATIONSHIP)
            .first.tx.outRefsOfType<RelationshipState<DummyConfig>>().single()
        val newRelationship = oldRelationship.getNextOutput()
        val (transaction) = bob.amendRelationship(oldRelationship, newRelationship)

        // Assert
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `AmendRelationshipFlow transaction should be signed by all participants (initiated by operator, centralized)`() {

        // Act
        val oldRelationship = alice.issueRelationship(CENTRALIZED_RELATIONSHIP)
            .first.tx.outRefsOfType<RelationshipState<DummyConfig>>().single()
        val newRelationship = oldRelationship.getNextOutput()
        val (transaction) = operator.amendRelationship(oldRelationship, newRelationship)

        // Assert
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `AmendRelationshipFlow transaction should be signed by all participants (initiated by alice, decentralized)`() {

        // Act
        val oldRelationship = alice.issueRelationship(DECENTRALIZED_RELATIONSHIP)
            .first.tx.outRefsOfType<RelationshipState<DummyConfig>>().single()
        val newRelationship = oldRelationship.getNextOutput()
        val (transaction) = alice.amendRelationship(oldRelationship, newRelationship)

        // Assert
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `AmendRelationshipFlow transaction should be signed by all participants (initiated by bob, decentralized)`() {

        // Act
        val oldRelationship = alice.issueRelationship(DECENTRALIZED_RELATIONSHIP)
            .first.tx.outRefsOfType<RelationshipState<DummyConfig>>().single()
        val newRelationship = oldRelationship.getNextOutput()
        val (transaction) = bob.amendRelationship(oldRelationship, newRelationship)

        // Assert
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `AmendRelationshipFlow should record a transaction for all participants (centralized)`() {

        // Act
        val oldRelationship = alice.issueRelationship(CENTRALIZED_RELATIONSHIP)
            .first.tx.outRefsOfType<RelationshipState<DummyConfig>>().single()
        val newRelationship = oldRelationship.getNextOutput()
        val (transaction, relationship) = alice.amendRelationship(oldRelationship, newRelationship)

        listOf(alice, bob, charlie, operator).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Could not find a recorded transaction with id: ${transaction.id}.")

                assertEquals(transaction, recordedTransaction)
                assertEquals(1, recordedTransaction.tx.inputs.size)
                assertEquals(1, recordedTransaction.tx.outputs.size)

                val recordedRelationship = recordedTransaction
                    .tx.outputsOfType<RelationshipState<DummyConfig>>().single()

                assertEquals(relationship, recordedRelationship)
            }
        }
    }

    @Test
    fun `AmendRelationshipFlow should record a transaction for all participants (decentralized)`() {

        // Act
        val oldRelationship = alice.issueRelationship(DECENTRALIZED_RELATIONSHIP)
            .first.tx.outRefsOfType<RelationshipState<DummyConfig>>().single()
        val newRelationship = oldRelationship.getNextOutput()
        val (transaction, relationship) = alice.amendRelationship(oldRelationship, newRelationship)

        listOf(alice, bob, charlie).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Could not find a recorded transaction with id: ${transaction.id}.")

                assertEquals(transaction, recordedTransaction)
                assertEquals(1, recordedTransaction.tx.inputs.size)
                assertEquals(1, recordedTransaction.tx.outputs.size)

                val recordedRelationship = recordedTransaction
                    .tx.outputsOfType<RelationshipState<DummyConfig>>().single()

                assertEquals(relationship, recordedRelationship)
            }
        }
    }
}