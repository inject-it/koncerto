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

@DisplayName("Decentralized membership revocation tests")
class RevokeMembershipFlowTests : MockNetworkFlowTest() {

    private fun initialize(initiator: StartedMockNode): SignedTransaction {
        val oldMembership = alice.createMembership(DECENTRALIZED_NETWORK, setOf(bob.party, charlie.party))
            .tx.outRefsOfType<MembershipState<DummyIdentity>>().single()

        return initiator.revokeMembership(oldMembership, setOf(bob.party, charlie.party))
    }

    @Test
    fun `RevokeMembershipFlow transaction should be signed by the initiator`() {

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
}