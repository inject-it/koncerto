package io.cordacity.koncerto.workflow.membership.decentralized

import io.cordacity.koncerto.contract.AttestationStatus
import io.cordacity.koncerto.contract.membership.MembershipAttestationState
import io.cordacity.koncerto.contract.membership.MembershipState
import io.cordacity.koncerto.workflow.DummyIdentity
import io.cordacity.koncerto.workflow.MockNetworkFlowTest
import net.corda.core.transactions.SignedTransaction
import net.corda.testing.node.StartedMockNode
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.test.assertEquals

@DisplayName("Decentralized membership attestation revocation tests")
class RevokeMembershipAttestationFlowTests : MockNetworkFlowTest() {

    private fun initialize(initiator: StartedMockNode): SignedTransaction {
        val membership = alice.createMembership(DECENTRALIZED_NETWORK, setOf(bob.party, charlie.party))
            .tx.outRefsOfType<MembershipState<DummyIdentity>>().single()

        val attestation = initiator.issueMembershipAttestation(
            MembershipAttestationState.create(initiator.party, membership, AttestationStatus.ACCEPTED)
        ).tx.outRefsOfType<MembershipAttestationState>().single()

        return initiator.revokeMembershipAttestation(attestation)
    }

    @Test
    fun `RevokeMembershipAttestationFlow transaction should be signed by the initiator`() {

        // Act
        val transaction = initialize(bob)

        // Assert
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `RevokeMembershipAttestationFlow should record a transaction for all participants`() {

        // Act
        val transaction = initialize(bob)

        // Assert
        listOf(alice, bob).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Could not find a recorded transaction with id: ${transaction.id}.")

                assertEquals(transaction, recordedTransaction)
                assertEquals(1, recordedTransaction.tx.inputs.size)
            }
        }
    }
}