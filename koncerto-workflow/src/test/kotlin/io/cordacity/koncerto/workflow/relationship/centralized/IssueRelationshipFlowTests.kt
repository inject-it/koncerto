package io.cordacity.koncerto.workflow.relationship.centralized

import io.cordacity.koncerto.contract.relationship.RelationshipState
import io.cordacity.koncerto.contract.revocation.RevocationLockState
import io.cordacity.koncerto.workflow.DummyConfig
import io.cordacity.koncerto.workflow.MockNetworkFlowTest
import net.corda.core.transactions.SignedTransaction
import net.corda.testing.node.StartedMockNode
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.test.assertEquals

@DisplayName("Centralized relationship issuance tests (no membership checking)")
class IssueRelationshipFlowTests : MockNetworkFlowTest() {

    private fun initialize(initiator: StartedMockNode): Pair<SignedTransaction, RelationshipState<DummyConfig>> {
        val transaction = initiator.issueRelationship(CENTRALIZED_RELATIONSHIP)
        val relationship = transaction.tx.outputsOfType<RelationshipState<DummyConfig>>().single()

        return transaction to relationship
    }

    @Test
    fun `IssueRelationshipFlow transaction should be signed by all participants (issued by member)`() {


        // Act
        val (transaction, _) = initialize(alice)

        // Assert
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `IssueRelationshipFlow transaction should be signed by all participants (issued by operator)`() {


        // Act
        val (transaction, _) = initialize(operator)

        // Assert
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `IssueRelationshipFlow should record a transaction for all participants`() {

        // Act
        val (transaction, relationship) = initialize(alice)

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
}