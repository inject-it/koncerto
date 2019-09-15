package io.cordacity.koncerto.workflow.membership.decentralized

import io.cordacity.koncerto.contract.membership.MembershipState
import io.cordacity.koncerto.workflow.DummyIdentity
import io.cordacity.koncerto.workflow.MockNetworkFlowTest
import net.corda.core.transactions.SignedTransaction
import net.corda.testing.node.StartedMockNode
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.test.assertEquals

@DisplayName("Decentralized membership issuance tests")
class IssueMembershipFlowTests : MockNetworkFlowTest() {

    private fun initialize(initiator: StartedMockNode): Pair<SignedTransaction, MembershipState<DummyIdentity>> {
        val transaction = initiator.createMembership(DECENTRALIZED_NETWORK, setOf(bob.party, charlie.party))
        val membership = transaction.tx.outputsOfType<MembershipState<DummyIdentity>>().single()

        return transaction to membership
    }

    @Test
    fun `IssueMembershipFlow transaction should be signed by the initiator`() {

        // Act
        val (transaction, _) = initialize(alice)

        // Assert
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `IssueMembershipFlow should record a transaction for all participants`() {

        // Act
        val (transaction, membership) = initialize(alice)

        // Assert
        listOf(alice, bob, charlie).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Could not find a recorded transaction with id: ${transaction.id}.")

                assertEquals(transaction, recordedTransaction)
                assertEquals(0, recordedTransaction.tx.inputs.size)
                assertEquals(1, recordedTransaction.tx.outputs.size)

                val recordedMembership = recordedTransaction
                    .tx.outputsOfType<MembershipState<DummyIdentity>>().single()

                assertEquals(membership, recordedMembership)
            }
        }
    }
}