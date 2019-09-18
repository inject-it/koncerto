package io.cordacity.koncerto.workflow.revocation

import io.cordacity.koncerto.contract.revocation.RevocationLockState
import io.cordacity.koncerto.workflow.DummyLinearState
import io.cordacity.koncerto.workflow.MockNetworkFlowTest
import net.corda.core.contracts.UniqueIdentifier
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.util.*
import kotlin.test.assertEquals

class CreateRevocationLockFlowTests : MockNetworkFlowTest() {

    @Test
    fun `CreateRevocationLockFlow transaction should be signed by the owner`() {

        // Arrange
        val revocationLock = RevocationLockState.create(alice.party, DummyLinearState())

        // Act
        val transaction = alice.createRevocationLock(revocationLock)

        // Assert
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `CreateRevocationLockFlow should record a transaction for the owner`() {

        // Arrange
        val revocationLock = RevocationLockState.create(alice.party, DummyLinearState())

        // Act
        val transaction = alice.createRevocationLock(revocationLock)

        // Assert
        alice.transaction {
            val recordedTransaction = alice.services.validatedTransactions.getTransaction(transaction.id)
                ?: fail("Could not find a recorded transaction with id: ${transaction.id}.")

            assertEquals(transaction, recordedTransaction)
            assertEquals(0, recordedTransaction.tx.inputs.size)
            assertEquals(1, recordedTransaction.tx.outputs.size)

            val recordedRevocationLock = recordedTransaction
                .tx.outputsOfType<RevocationLockState<DummyLinearState>>().single()

            assertEquals(revocationLock, recordedRevocationLock)
        }
    }
}