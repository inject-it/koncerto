package io.cordacity.koncerto.workflow.revocation

import io.cordacity.koncerto.contract.revocation.RevocationLockState
import io.cordacity.koncerto.contract.revocation.RevocationLockStatus
import io.cordacity.koncerto.workflow.DummyLinearState
import io.cordacity.koncerto.workflow.MockNetworkFlowTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.test.assertEquals

class UpdateRevocationLockFlowTests : MockNetworkFlowTest() {

    @Test
    fun `UpdateRevocationLockFlow transaction should be signed by the owner`() {

        // Arrange
        val oldRevocationLock = alice.createRevocationLock(RevocationLockState.create(alice.party, DummyLinearState()))
            .tx.outRefsOfType<RevocationLockState<DummyLinearState>>().single()

        // Act
        val transaction = alice.updateRevocationLock(oldRevocationLock, RevocationLockStatus.UNLOCKED)

        // Assert
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `UpdateRevocationLockFlow should record a transaction for the owner`() {

        // Arrange
        val oldRevocationLock = alice.createRevocationLock(RevocationLockState.create(alice.party, DummyLinearState()))
            .tx.outRefsOfType<RevocationLockState<DummyLinearState>>().single()

        // Act
        val transaction = alice.updateRevocationLock(oldRevocationLock, RevocationLockStatus.UNLOCKED)

        // Assert
        alice.transaction {
            val recordedTransaction = alice.services.validatedTransactions.getTransaction(transaction.id)
                ?: fail("Could not find a recorded transaction with id: ${transaction.id}.")

            assertEquals(transaction, recordedTransaction)
            assertEquals(1, recordedTransaction.tx.inputs.size)
            assertEquals(1, recordedTransaction.tx.outputs.size)

            val recordedRevocationLock = recordedTransaction
                .tx.outputsOfType<RevocationLockState<DummyLinearState>>().single()

            assertEquals(RevocationLockStatus.UNLOCKED, recordedRevocationLock.status)
        }
    }
}