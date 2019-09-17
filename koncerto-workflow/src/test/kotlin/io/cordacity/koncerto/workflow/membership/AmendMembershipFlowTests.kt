package io.cordacity.koncerto.workflow.membership

import io.cordacity.koncerto.contract.getNextOutput
import io.cordacity.koncerto.contract.membership.MembershipState
import io.cordacity.koncerto.workflow.DummyIdentity
import io.cordacity.koncerto.workflow.MockNetworkFlowTest
import net.corda.core.transactions.SignedTransaction
import net.corda.testing.node.StartedMockNode
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.test.assertEquals

class AmendMembershipFlowTests : MockNetworkFlowTest() {

    @Test
    fun `AmendMembershipFlow transaction should be signed by the initiator (member amendment, centralized)`() {

        // Act
        val oldMembership = alice.issueMembership(CENTRALIZED_NETWORK, setOf(operator.party))
            .first.tx.outRefsOfType<MembershipState<DummyIdentity>>().single()
        val newMembership = oldMembership.getNextOutput().addRoles("Power User")
        val (transaction) = alice.amendMembership(oldMembership, newMembership, setOf(operator.party))

        // Assert
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `AmendMembershipFlow transaction should be signed by the initiator (operator amendment, centralized)`() {

        // Act
        // Act
        val oldMembership = alice.issueMembership(CENTRALIZED_NETWORK, setOf(operator.party))
            .first.tx.outRefsOfType<MembershipState<DummyIdentity>>().single()
        val newMembership = oldMembership.getNextOutput().addRoles("Power User")
        val (transaction) = operator.amendMembership(oldMembership, newMembership, setOf(alice.party))

        // Assert
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `AmendMembershipFlow transaction should be signed by the initiator (member amendment, decentralized)`() {

        // Act
        // Act
        val oldMembership = alice.issueMembership(DECENTRALIZED_NETWORK, setOf(bob.party, charlie.party))
            .first.tx.outRefsOfType<MembershipState<DummyIdentity>>().single()
        val newMembership = oldMembership.getNextOutput().addRoles("Power User")
        val (transaction) = alice.amendMembership(oldMembership, newMembership, setOf(bob.party, charlie.party))

        // Assert
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `AmendMembershipFlow should record a transaction for all participants (centralized)`() {

        // Act
        val oldMembership = alice.issueMembership(CENTRALIZED_NETWORK, setOf(operator.party))
            .first.tx.outRefsOfType<MembershipState<DummyIdentity>>().single()
        val newMembership = oldMembership.getNextOutput().addRoles("Power User")
        val (transaction) = alice.amendMembership(oldMembership, newMembership, setOf(operator.party))

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

                assertEquals(newMembership, recordedMembership)
                assert(recordedMembership.hasRole("Power User"))
            }
        }
    }

    @Test
    fun `AmendMembershipFlow should record a transaction for all participants (decentralized)`() {

        // Act
        val oldMembership = alice.issueMembership(DECENTRALIZED_NETWORK, setOf(bob.party, charlie.party))
            .first.tx.outRefsOfType<MembershipState<DummyIdentity>>().single()
        val newMembership = oldMembership.getNextOutput().addRoles("Power User")
        val (transaction) = alice.amendMembership(oldMembership, newMembership, setOf(bob.party, charlie.party))

        // Assert
        listOf(alice, bob, charlie).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Could not find a recorded transaction with id: ${transaction.id}.")

                assertEquals(transaction, recordedTransaction)
                assertEquals(1, recordedTransaction.tx.inputs.size)
                assertEquals(1, recordedTransaction.tx.outputs.size)

                val recordedMembership = recordedTransaction
                    .tx.outputsOfType<MembershipState<DummyIdentity>>().single()

                assertEquals(newMembership, recordedMembership)
                assert(recordedMembership.hasRole("Power User"))
            }
        }
    }
}