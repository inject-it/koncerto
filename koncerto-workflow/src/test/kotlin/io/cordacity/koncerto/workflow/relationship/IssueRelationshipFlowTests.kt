package io.cordacity.koncerto.workflow.relationship

import io.cordacity.koncerto.contract.relationship.RelationshipState
import io.cordacity.koncerto.contract.revocation.RevocationLockState
import io.cordacity.koncerto.workflow.DummyConfig
import io.cordacity.koncerto.workflow.MockNetworkFlowTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.test.assertEquals

class IssueRelationshipFlowTests : MockNetworkFlowTest() {

    @Test
    fun `IssueRelationshipFlow transaction should be signed by all participants (issued by member, centralized)`() {

        // Act
        val (transaction) = alice.issueRelationship(CENTRALIZED_RELATIONSHIP)

        // Assert
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `IssueRelationshipFlow transaction should be signed by all participants (issued by operator, centralized)`() {

        // Act
        val (transaction) = operator.issueRelationship(CENTRALIZED_RELATIONSHIP)

        // Assert
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `IssueRelationshipFlow transaction should be signed by all participants (issued by member, decentralized)`() {

        // Act
        val (transaction) = alice.issueRelationship(DECENTRALIZED_RELATIONSHIP)

        // Assert
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `IssueRelationshipFlow should record a transaction for all participants (centralized)`() {

        // Act
        val (transaction, relationship) = alice.issueRelationship(CENTRALIZED_RELATIONSHIP)

        // Assert
        listOf(alice, bob, charlie, operator).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Could not find a recorded transaction with id: ${transaction.id}.")

                assertEquals(transaction, recordedTransaction)
                assertEquals(0, recordedTransaction.tx.inputs.size)
                assertEquals(5, recordedTransaction.tx.outputs.size)

                val recordedRelationship = recordedTransaction
                    .tx.outputsOfType<RelationshipState<DummyConfig>>().single()

                val recordedRevocationLocks = recordedTransaction
                    .tx.outputsOfType<RevocationLockState<*>>()

                assertEquals(relationship, recordedRelationship)
                assertEquals(4, recordedRevocationLocks.size)
            }
        }
    }

    @Test
    fun `IssueRelationshipFlow should record a transaction for all participants (decentralized)`() {

        // Act
        val (transaction, relationship) = alice.issueRelationship(DECENTRALIZED_RELATIONSHIP)

        // Assert
        listOf(alice, bob, charlie).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Could not find a recorded transaction with id: ${transaction.id}.")

                assertEquals(transaction, recordedTransaction)
                assertEquals(0, recordedTransaction.tx.inputs.size)
                assertEquals(4, recordedTransaction.tx.outputs.size)

                val recordedRelationship = recordedTransaction
                    .tx.outputsOfType<RelationshipState<DummyConfig>>().single()

                val recordedRevocationLocks = recordedTransaction
                    .tx.outputsOfType<RevocationLockState<*>>()

                assertEquals(relationship, recordedRelationship)
                assertEquals(3, recordedRevocationLocks.size)
            }
        }
    }
}