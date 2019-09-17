package io.cordacity.koncerto.workflow.membership

import io.cordacity.koncerto.contract.membership.MembershipState
import io.cordacity.koncerto.workflow.DummyIdentity
import io.cordacity.koncerto.workflow.MockNetworkFlowTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.test.assertEquals
import kotlin.test.assertFails

class IssueMembershipFlowTests : MockNetworkFlowTest() {

    @Test
    fun `IssueMembershipFlow transaction should be signed by the initiator (centralized)`() {

        // Act
        val (transaction) = alice.issueMembership(CENTRALIZED_NETWORK, setOf(operator.party))

        // Assert
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `IssueMembershipFlow transaction should be signed by the initiator (decentralized)`() {

        // Act
        val (transaction) = alice.issueMembership(DECENTRALIZED_NETWORK, setOf(bob.party, charlie.party))

        // Assert
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `IssueMembershipFlow should record a transaction for all participants (centralized)`() {

        // Act
        val (transaction, membership) = alice.issueMembership(CENTRALIZED_NETWORK, setOf(operator.party))

        // Assert
        listOf(alice, operator).forEach {
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

    @Test
    fun `IssueMembershipFlow should record a transaction for all participants (decentralized)`() {

        // Act
        val (transaction, membership) = alice.issueMembership(DECENTRALIZED_NETWORK, setOf(bob.party, charlie.party))

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

    @Test
    fun `IssueMembershipFlow should fail on attempts to create duplicate membership states (centralized)`() {
        assertFails {
            alice.issueMembership(DECENTRALIZED_NETWORK, setOf(bob.party, charlie.party))
            alice.issueMembership(DECENTRALIZED_NETWORK, setOf(bob.party, charlie.party))
        }
    }

    @Test
    fun `IssueMembershipFlow should fail on attempts to create duplicate membership states (decentralized)`() {
        assertFails {
            alice.issueMembership(DECENTRALIZED_NETWORK, setOf(bob.party, charlie.party))
            alice.issueMembership(DECENTRALIZED_NETWORK, setOf(bob.party, charlie.party))
        }
    }
}