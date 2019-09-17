package io.cordacity.koncerto.workflow.relationship

import io.cordacity.koncerto.contract.relationship.RelationshipState
import io.cordacity.koncerto.contract.revocation.RevocationLockStatus
import io.cordacity.koncerto.workflow.DummyConfig
import io.cordacity.koncerto.workflow.MockNetworkFlowTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.test.assertEquals
import kotlin.test.assertFails

class RevokeRelationshipFlowTests : MockNetworkFlowTest() {

    @Test
    fun `RevokeRelationshipFlow transaction should be signed by all participants (initiated by alice, centralized)`() {

        // Act
        val (issuanceTransaction, _, revocationLocks) = alice.issueRelationship(CENTRALIZED_RELATIONSHIP)
        val oldRelationship = issuanceTransaction.tx.outRefsOfType<RelationshipState<DummyConfig>>().single()

        listOf(alice, bob, charlie, operator).forEach { node ->
            val revocationLock = revocationLocks.singleOrNull { it.state.data.owner == node.party }
                ?: fail("Could not find a revocation lock for participant: ${node.party}.")

            node.updateRevocationLock(revocationLock, RevocationLockStatus.UNLOCKED)
        }

        val transaction = alice.revokeRelationship(oldRelationship)

        // Assert
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `RevokeRelationshipFlow transaction should be signed by all participants (initiated by bob, centralized)`() {

        // Act
        val (issuanceTransaction, _, revocationLocks) = alice.issueRelationship(CENTRALIZED_RELATIONSHIP)
        val oldRelationship = issuanceTransaction.tx.outRefsOfType<RelationshipState<DummyConfig>>().single()

        listOf(alice, bob, charlie, operator).forEach { node ->
            val revocationLock = revocationLocks.singleOrNull { it.state.data.owner == node.party }
                ?: fail("Could not find a revocation lock for participant: ${node.party}.")

            node.updateRevocationLock(revocationLock, RevocationLockStatus.UNLOCKED)
        }

        val transaction = bob.revokeRelationship(oldRelationship)

        // Assert
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `RevokeRelationshipFlow transaction should be signed by all participants (initiated by operator, centralized)`() {

        // Act
        val (issuanceTransaction, _, revocationLocks) = alice.issueRelationship(CENTRALIZED_RELATIONSHIP)
        val oldRelationship = issuanceTransaction.tx.outRefsOfType<RelationshipState<DummyConfig>>().single()

        listOf(alice, bob, charlie, operator).forEach { node ->
            val revocationLock = revocationLocks.singleOrNull { it.state.data.owner == node.party }
                ?: fail("Could not find a revocation lock for participant: ${node.party}.")

            node.updateRevocationLock(revocationLock, RevocationLockStatus.UNLOCKED)
        }

        val transaction = operator.revokeRelationship(oldRelationship)

        // Assert
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `RevokeRelationshipFlow transaction should be signed by all participants (initiated by alice, decentralized)`() {

        // Act
        val (issuanceTransaction, _, revocationLocks) = alice.issueRelationship(DECENTRALIZED_RELATIONSHIP)
        val oldRelationship = issuanceTransaction.tx.outRefsOfType<RelationshipState<DummyConfig>>().single()

        listOf(alice, bob, charlie).forEach { node ->
            val revocationLock = revocationLocks.singleOrNull { it.state.data.owner == node.party }
                ?: fail("Could not find a revocation lock for participant: ${node.party}.")

            node.updateRevocationLock(revocationLock, RevocationLockStatus.UNLOCKED)
        }

        val transaction = alice.revokeRelationship(oldRelationship)

        // Assert
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `RevokeRelationshipFlow transaction should be signed by all participants (initiated by bob, decentralized)`() {

        // Act
        val (issuanceTransaction, _, revocationLocks) = alice.issueRelationship(DECENTRALIZED_RELATIONSHIP)
        val oldRelationship = issuanceTransaction.tx.outRefsOfType<RelationshipState<DummyConfig>>().single()

        listOf(alice, bob, charlie).forEach { node ->
            val revocationLock = revocationLocks.singleOrNull { it.state.data.owner == node.party }
                ?: fail("Could not find a revocation lock for participant: ${node.party}.")

            node.updateRevocationLock(revocationLock, RevocationLockStatus.UNLOCKED)
        }

        val transaction = bob.revokeRelationship(oldRelationship)

        // Assert
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `AmendRelationshipFlow should record a transaction for all participants (centralized)`() {

        // Act
        val (issuanceTransaction, _, revocationLocks) = alice.issueRelationship(CENTRALIZED_RELATIONSHIP)
        val oldRelationship = issuanceTransaction.tx.outRefsOfType<RelationshipState<DummyConfig>>().single()

        listOf(alice, bob, charlie, operator).forEach { node ->
            val revocationLock = revocationLocks.singleOrNull { it.state.data.owner == node.party }
                ?: fail("Could not find a revocation lock for participant: ${node.party}.")

            node.updateRevocationLock(revocationLock, RevocationLockStatus.UNLOCKED)
        }

        val transaction = alice.revokeRelationship(oldRelationship)

        listOf(alice, bob, charlie, operator).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Could not find a recorded transaction with id: ${transaction.id}.")

                assertEquals(transaction, recordedTransaction)
                assertEquals(1, recordedTransaction.tx.inputs.size)
                assertEquals(0, recordedTransaction.tx.outputs.size)
            }
        }
    }

    @Test
    fun `AmendRelationshipFlow should record a transaction for all participants (centralized, locks have been deleted)`() {

        // Act
        val (issuanceTransaction, _, revocationLocks) = alice.issueRelationship(CENTRALIZED_RELATIONSHIP)
        val oldRelationship = issuanceTransaction.tx.outRefsOfType<RelationshipState<DummyConfig>>().single()

        listOf(alice, bob, charlie, operator).forEach { node ->
            val revocationLock = revocationLocks.singleOrNull { it.state.data.owner == node.party }
                ?: fail("Could not find a revocation lock for participant: ${node.party}.")

            node.deleteRevocationLock(revocationLock)
        }

        val transaction = alice.revokeRelationship(oldRelationship)

        listOf(alice, bob, charlie, operator).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Could not find a recorded transaction with id: ${transaction.id}.")

                assertEquals(transaction, recordedTransaction)
                assertEquals(1, recordedTransaction.tx.inputs.size)
                assertEquals(0, recordedTransaction.tx.outputs.size)
            }
        }
    }

    @Test
    fun `RevokeRelationshipFlow should record a transaction for all participants (decentralized)`() {

        // Act
        val (issuanceTransaction, _, revocationLocks) = alice.issueRelationship(DECENTRALIZED_RELATIONSHIP)
        val oldRelationship = issuanceTransaction.tx.outRefsOfType<RelationshipState<DummyConfig>>().single()

        listOf(alice, bob, charlie).forEach { node ->
            val revocationLock = revocationLocks.singleOrNull { it.state.data.owner == node.party }
                ?: fail("Could not find a revocation lock for participant: ${node.party}.")

            node.updateRevocationLock(revocationLock, RevocationLockStatus.UNLOCKED)
        }

        val transaction = alice.revokeRelationship(oldRelationship)

        listOf(alice, bob, charlie).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Could not find a recorded transaction with id: ${transaction.id}.")

                assertEquals(transaction, recordedTransaction)
                assertEquals(1, recordedTransaction.tx.inputs.size)
                assertEquals(0, recordedTransaction.tx.outputs.size)
            }
        }
    }

    @Test
    fun `RevokeRelationshipFlow should record a transaction for all participants (decentralized, locks have been deleted)`() {

        // Act
        val (issuanceTransaction, _, revocationLocks) = alice.issueRelationship(DECENTRALIZED_RELATIONSHIP)
        val oldRelationship = issuanceTransaction.tx.outRefsOfType<RelationshipState<DummyConfig>>().single()

        listOf(alice, bob, charlie).forEach { node ->
            val revocationLock = revocationLocks.singleOrNull { it.state.data.owner == node.party }
                ?: fail("Could not find a revocation lock for participant: ${node.party}.")

            node.deleteRevocationLock(revocationLock)
        }

        val transaction = alice.revokeRelationship(oldRelationship)

        listOf(alice, bob, charlie).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Could not find a recorded transaction with id: ${transaction.id}.")

                assertEquals(transaction, recordedTransaction)
                assertEquals(1, recordedTransaction.tx.inputs.size)
                assertEquals(0, recordedTransaction.tx.outputs.size)
            }
        }
    }

    @Test
    fun `AmendRelationshipFlow should fail if there are locked revocation locks (centralized)`() {

        // Act
        val (issuanceTransaction, _, revocationLocks) = alice.issueRelationship(CENTRALIZED_RELATIONSHIP)
        val oldRelationship = issuanceTransaction.tx.outRefsOfType<RelationshipState<DummyConfig>>().single()

        listOf(alice, bob, operator).forEach { node ->
            val revocationLock = revocationLocks.singleOrNull { it.state.data.owner == node.party }
                ?: fail("Could not find a revocation lock for participant: ${node.party}.")

            node.updateRevocationLock(revocationLock, RevocationLockStatus.UNLOCKED)
        }

        // Assert
        assertFails {
            alice.revokeRelationship(oldRelationship)
        }
    }

    @Test
    fun `AmendRelationshipFlow should fail if there are locked revocation locks (decentralized)`() {

        // Act
        val (issuanceTransaction, _, revocationLocks) = alice.issueRelationship(DECENTRALIZED_RELATIONSHIP)
        val oldRelationship = issuanceTransaction.tx.outRefsOfType<RelationshipState<DummyConfig>>().single()

        listOf(alice, bob).forEach { node ->
            val revocationLock = revocationLocks.singleOrNull { it.state.data.owner == node.party }
                ?: fail("Could not find a revocation lock for participant: ${node.party}.")

            node.updateRevocationLock(revocationLock, RevocationLockStatus.UNLOCKED)
        }

        // Assert
        assertFails {
            alice.revokeRelationship(oldRelationship)
        }
    }
}