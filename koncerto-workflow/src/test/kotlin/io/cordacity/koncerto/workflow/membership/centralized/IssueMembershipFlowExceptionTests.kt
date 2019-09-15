package io.cordacity.koncerto.workflow.membership.centralized

import io.cordacity.koncerto.contract.membership.MembershipState
import io.cordacity.koncerto.workflow.DummyIdentity
import io.cordacity.koncerto.workflow.MockNetworkFlowTest
import net.corda.core.transactions.SignedTransaction
import net.corda.testing.node.StartedMockNode
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertFails

@DisplayName("Centralized membership issuance exception tests")
class IssueMembershipFlowExceptionTests : MockNetworkFlowTest() {

    private fun initialize(initiator: StartedMockNode): Pair<SignedTransaction, MembershipState<DummyIdentity>> {
        val transaction = initiator.createMembership(CENTRALIZED_NETWORK, setOf(operator.party))
        val membership = transaction.tx.outputsOfType<MembershipState<DummyIdentity>>().single()

        return transaction to membership
    }

    @Test
    fun `IssueMembershipFlow cannot create duplicate memberships on the same network`() {
        assertFails {
            initialize(alice)
            initialize(alice)
        }
    }
}