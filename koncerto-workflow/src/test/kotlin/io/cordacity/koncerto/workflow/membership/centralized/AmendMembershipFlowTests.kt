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

@DisplayName("Centralized membership amendment tests")
class AmendMembershipFlowTests : MockNetworkFlowTest() {

    private fun initialize(initiator: StartedMockNode): Pair<SignedTransaction, MembershipState<DummyIdentity>> {
        val observers = setOf(alice.party, operator.party) - initiator.party

        val oldMembership = alice.createMembership(CENTRALIZED_NETWORK, setOf(operator.party))
            .tx.outRefsOfType<MembershipState<DummyIdentity>>().single()

        val membership = oldMembership.state.data.addRoles("Power User")
        val transaction = initiator.amendMembership(oldMembership, membership, observers)

        return transaction to membership
    }

    @Test
    fun `AmendMembershipFlow transaction should be signed by the initiator (member amendment)`() {

        // Act
        val (transaction, _) = initialize(alice)

        // Assert
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `AmendMembershipFlow transaction should be signed by the initiator (operator amendment)`() {

        // Act
        val (transaction, _) = initialize(operator)

        // Assert
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `AmendMembershipFlow should record a transaction for all participants`() {

        // Act
        val (transaction, membership) = initialize(alice)

        // Assert
        listOf(alice, operator).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Could not find a recorded transaction with id: ${transaction.id}.")

                assertEquals(transaction, recordedTransaction)
                assertEquals(1, recordedTransaction.tx.inputs.size)
                assertEquals(1, recordedTransaction.tx.outputs.size)

                val recordedMembership = recordedTransaction
                    .tx.outputsOfType<MembershipState<DummyIdentity>>().single()

                assertEquals(membership, recordedMembership)
                assert(recordedMembership.hasRole("Power User"))
            }
        }
    }
}