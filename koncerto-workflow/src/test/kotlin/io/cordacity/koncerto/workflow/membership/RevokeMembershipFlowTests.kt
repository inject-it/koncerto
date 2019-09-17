package io.cordacity.koncerto.workflow.membership

import io.cordacity.koncerto.contract.membership.MembershipState
import io.cordacity.koncerto.workflow.DummyIdentity
import io.cordacity.koncerto.workflow.MockNetworkFlowTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.test.assertEquals

class RevokeMembershipFlowTests : MockNetworkFlowTest() {

    @Test
    fun `RevokeMembershipFlow transaction should be signed by the initiator (member revocation, centralized)`() {

        // Act
        val oldMembership = alice.issueMembership(CENTRALIZED_NETWORK, setOf(operator.party))
            .first.tx.outRefsOfType<MembershipState<DummyIdentity>>().single()
        val transaction = alice.revokeMembership(oldMembership, setOf(operator.party))

        // Assert
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `RevokeMembershipFlow transaction should be signed by the initiator (operator revocation, centralized)`() {

        // Act
        val oldMembership = alice.issueMembership(CENTRALIZED_NETWORK, setOf(operator.party))
            .first.tx.outRefsOfType<MembershipState<DummyIdentity>>().single()
        val transaction = operator.revokeMembership(oldMembership, setOf(alice.party))

        // Assert
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `RevokeMembershipFlow transaction should be signed by the initiator (member revocation, decentralized)`() {

        // Act
        val oldMembership = alice.issueMembership(DECENTRALIZED_NETWORK, setOf(bob.party, charlie.party))
            .first.tx.outRefsOfType<MembershipState<DummyIdentity>>().single()
        val transaction = alice.revokeMembership(oldMembership, setOf(bob.party, charlie.party))

        // Assert
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `RevokeMembershipFlow should record a transaction for all participants (centralized)`() {

        // Act
        val oldMembership = alice.issueMembership(CENTRALIZED_NETWORK, setOf(operator.party))
            .first.tx.outRefsOfType<MembershipState<DummyIdentity>>().single()
        val transaction = operator.revokeMembership(oldMembership, setOf(alice.party))

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

    @Test
    fun `RevokeMembershipFlow should record a transaction for all participants (decentralized)`() {

        // Act
        val oldMembership = alice.issueMembership(DECENTRALIZED_NETWORK, setOf(bob.party, charlie.party))
            .first.tx.outRefsOfType<MembershipState<DummyIdentity>>().single()
        val transaction = alice.revokeMembership(oldMembership, setOf(bob.party, charlie.party))

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