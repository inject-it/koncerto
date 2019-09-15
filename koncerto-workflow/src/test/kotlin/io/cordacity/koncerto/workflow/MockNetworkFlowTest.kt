package io.cordacity.koncerto.workflow

import io.cordacity.koncerto.contract.Network
import io.cordacity.koncerto.contract.Role
import io.cordacity.koncerto.contract.membership.MembershipAttestationState
import io.cordacity.koncerto.contract.membership.MembershipState
import io.cordacity.koncerto.contract.relationship.RelationshipState
import io.cordacity.koncerto.contract.revocation.RevocationLockState
import io.cordacity.koncerto.contract.revocation.RevocationLockStatus
import io.cordacity.koncerto.workflow.membership.*
import io.cordacity.koncerto.workflow.relationship.*
import io.cordacity.koncerto.workflow.revocation.CreateRevocationLockFlow
import io.cordacity.koncerto.workflow.revocation.DeleteRevocationLockFlow
import io.cordacity.koncerto.workflow.revocation.UpdateRevocationLockFlow
import net.corda.core.concurrent.CordaFuture
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.StateAndRef
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.utilities.getOrThrow
import net.corda.testing.common.internal.testNetworkParameters
import net.corda.testing.core.singleIdentity
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkParameters
import net.corda.testing.node.StartedMockNode
import net.corda.testing.node.TestCordapp
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

abstract class MockNetworkFlowTest {

    val CENTRALIZED_NETWORK by lazy { Network("Centralized Network", operator.party) }
    val DECENTRALIZED_NETWORK by lazy { Network("Decentralized Network") }

    val RELATIONSHIP_CONFIGURATION by lazy {
        DummyConfig(
            "Test Relationship",
            setOf(alice.party, bob.party, charlie.party)
        )
    }

    private lateinit var _network: MockNetwork
    val network: MockNetwork get() = _network

    private lateinit var _notary: StartedMockNode
    val notary: StartedMockNode get() = _notary

    private lateinit var _alice: StartedMockNode
    val alice: StartedMockNode get() = _alice

    private lateinit var _bob: StartedMockNode
    val bob: StartedMockNode get() = _bob

    private lateinit var _charlie: StartedMockNode
    val charlie: StartedMockNode get() = _charlie

    private lateinit var _operator: StartedMockNode
    val operator: StartedMockNode get() = _operator

    val StartedMockNode.party: Party get() = info.singleIdentity()

    fun <T> runNetwork(function: () -> CordaFuture<T>): T {
        val result = function()
        network.runNetwork()
        return result.getOrThrow()
    }

    @BeforeEach
    open fun setup() {
        _network = MockNetwork(
            MockNetworkParameters(
                cordappsForAllNodes = listOf(
                    TestCordapp.findCordapp("io.cordacity.koncerto.contract.revocation"),
                    TestCordapp.findCordapp("io.cordacity.koncerto.contract.membership"),
                    TestCordapp.findCordapp("io.cordacity.koncerto.contract.relationship")
                ),
                networkParameters = testNetworkParameters(
                    minimumPlatformVersion = 4
                )
            )
        )

        _notary = network.defaultNotaryNode
        _alice = network.createPartyNode(CordaX500Name("Alice", "London", "GB"))
        _bob = network.createPartyNode(CordaX500Name("Bob", "New York", "US"))
        _charlie = network.createPartyNode(CordaX500Name("Charlie", "Paris", "FR"))
        _operator = network.createPartyNode(CordaX500Name("Operator", "London", "GB"))

        listOf(alice, bob, charlie, operator).forEach {
            it.registerInitiatedFlow(IssueMembershipFlow.Observer::class.java)
            it.registerInitiatedFlow(AmendMembershipFlow.Observer::class.java)
            it.registerInitiatedFlow(RevokeMembershipFlow.Observer::class.java)

            it.registerInitiatedFlow(IssueRelationshipFlow.Observer::class.java)
            it.registerInitiatedFlow(AmendRelationshipFlow.Observer::class.java)
            it.registerInitiatedFlow(RevokeRelationshipFlow.Observer::class.java)

            it.registerInitiatedFlow(IssueMembershipAttestationFlow.Observer::class.java)
            it.registerInitiatedFlow(AmendMembershipAttestationFlow.Observer::class.java)
            it.registerInitiatedFlow(RevokeMembershipAttestationFlow.Observer::class.java)

            it.registerInitiatedFlow(IssueRelationshipAttestationFlow.Observer::class.java)
            it.registerInitiatedFlow(AmendRelationshipAttestationFlow.Observer::class.java)
            it.registerInitiatedFlow(RevokeRelationshipAttestationFlow.Observer::class.java)
        }
    }

    @AfterEach
    open fun tearDown() {
        network.stopNodes()
    }

    fun StartedMockNode.createMembership(
        network: Network,
        observers: Set<Party> = emptySet(),
        roles: Set<Role> = emptySet()
    ) = runNetwork {
        val membership = MembershipState(network, DummyIdentity(party), roles)
        startFlow(IssueMembershipFlow.Initiator(membership, observers = observers))
    }

    fun StartedMockNode.amendMembership(
        oldMembership: StateAndRef<MembershipState<DummyIdentity>>,
        newMembership: MembershipState<DummyIdentity>,
        observers: Set<Party> = emptySet()
    ) = runNetwork {
        startFlow(AmendMembershipFlow.Initiator(oldMembership, newMembership, observers = observers))
    }

    fun StartedMockNode.revokeMembership(
        membership: StateAndRef<MembershipState<DummyIdentity>>,
        observers: Set<Party> = emptySet()
    ) = runNetwork {
        startFlow(RevokeMembershipFlow.Initiator(membership, observers = observers))
    }

    fun StartedMockNode.issueMembershipAttestation(
        attestation: MembershipAttestationState
    ) = runNetwork {
        startFlow(IssueMembershipAttestationFlow.Initiator(attestation))
    }

    fun StartedMockNode.amendMembershipAttestation(
        oldAttestation: StateAndRef<MembershipAttestationState>,
        newAttestation: MembershipAttestationState
    ) = runNetwork {
        startFlow(AmendMembershipAttestationFlow.Initiator(oldAttestation, newAttestation))
    }

    fun StartedMockNode.revokeMembershipAttestation(
        attestation: StateAndRef<MembershipAttestationState>
    ) = runNetwork {
        startFlow(RevokeMembershipAttestationFlow.Initiator(attestation))
    }

    fun StartedMockNode.issueRelationship(
        relationship: RelationshipState<DummyConfig>,
        checkMembership: Boolean = false
    ) = runNetwork {
        startFlow(IssueRelationshipFlow.Initiator(relationship, checkMembership = checkMembership))
    }

    fun StartedMockNode.amendRelationship(
        oldRelationship: StateAndRef<RelationshipState<DummyConfig>>,
        newRelationship: RelationshipState<DummyConfig>,
        checkMembership: Boolean = false
    ) = runNetwork {
        startFlow(AmendRelationshipFlow.Initiator(oldRelationship, newRelationship, checkMembership = checkMembership))
    }

    fun StartedMockNode.revokeRelationship(
        relationship: StateAndRef<RelationshipState<DummyConfig>>
    ) = runNetwork {
        startFlow(RevokeRelationshipFlow.Initiator(relationship))
    }

    fun StartedMockNode.createRevocationLock(
        state: LinearState
    ) = runNetwork {
        startFlow(CreateRevocationLockFlow(state))
    }

    fun StartedMockNode.updateRevocationLock(
        revocationLock: StateAndRef<RevocationLockState<*>>,
        status: RevocationLockStatus
    ) = runNetwork {
        startFlow(UpdateRevocationLockFlow(revocationLock, status))
    }

    fun StartedMockNode.deleteRevocationLock(
        revocationLock: StateAndRef<RevocationLockState<*>>
    ) = runNetwork {
        startFlow(DeleteRevocationLockFlow(revocationLock))
    }
}