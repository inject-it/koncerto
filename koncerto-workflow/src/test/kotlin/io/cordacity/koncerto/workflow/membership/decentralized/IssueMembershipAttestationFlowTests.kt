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

@DisplayName("Decentralized membership attestation issuance tests")
class IssueMembershipAttestationFlowTests : MockNetworkFlowTest() {

    private fun initialize(initiator: StartedMockNode): Pair<SignedTransaction, MembershipAttestationState> {
        val membership = alice.createMembership(DECENTRALIZED_NETWORK, setOf(bob.party, charlie.party))
            .tx.outRefsOfType<MembershipState<DummyIdentity>>().single()

        val attestation = MembershipAttestationState.create(initiator.party, membership, AttestationStatus.ACCEPTED)
        val transaction = initiator.issueMembershipAttestation(attestation)

        return transaction to attestation
    }

    @Test
    fun `IssueMembershipAttestationFlow transaction should be signed by the initiator`() {

        // Act
        val (transaction, _) = initialize(bob)

        // Assert
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `IssueMembershipAttestationFlow should record a transaction for all participants`() {

        // Act
        val (transaction, attestation) = initialize(bob)

        // Assert
        listOf(alice, bob).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Could not find a recorded transaction with id: ${transaction.id}.")

                assertEquals(transaction, recordedTransaction)
                assertEquals(0, recordedTransaction.tx.inputs.size)
                assertEquals(1, recordedTransaction.tx.references.size)
                assertEquals(1, recordedTransaction.tx.outputs.size)

                val recordedAttestation = recordedTransaction
                    .tx.outputsOfType<MembershipAttestationState>().single()

                assertEquals(attestation, recordedAttestation)
            }
        }
    }
}