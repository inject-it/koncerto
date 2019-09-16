package io.cordacity.koncerto.workflow.membership.centralized

import io.cordacity.koncerto.contract.membership.MembershipState
import io.cordacity.koncerto.workflow.DummyIdentity
import io.cordacity.koncerto.workflow.MockNetworkFlowTest
import net.corda.core.transactions.SignedTransaction
import net.corda.testing.node.StartedMockNode
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.test.assertEquals

@DisplayName("Centralized membership revocation tests")
class RevokeMembershipFlowTests : MockNetworkFlowTest() {

    private fun initialize(initiator: StartedMockNode): SignedTransaction {
        val observers = setOf(alice.party, operator.party) - initiator.party

        val oldMembership = alice.issueMembership(CENTRALIZED_NETWORK, setOf(operator.party))
            .tx.outRefsOfType<MembershipState<DummyIdentity>>().single()

        return initiator.revokeMembership(oldMembership, observers)
    }

    @Test
    fun `RevokeMembershipFlow transaction should be signed by the initiator (member revocation)`() {

        // Act
        val transaction = initialize(alice)

        // Assert
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `RevokeMembershipFlow transaction should be signed by the initiator (operator revocation)`() {

        // Act
        val transaction = initialize(alice)

        // Assert
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `RevokeMembershipFlow should record a transaction for all participants`() {

        // Act
        val transaction = initialize(alice)

        // Assert
        listOf(alice, operator).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Could not find a recorded transaction with id: ${transaction.id}.")

                assertEquals(transaction, recordedTransaction)
                assertEquals(1, recordedTransaction.tx.inputs.size)
                assertEquals(0, recordedTransaction.tx.outputs.size)
            }
        }
    }
}