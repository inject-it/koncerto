package io.cordacity.koncerto.contract

import io.cordacity.koncerto.contract.membership.MembershipAttestationContract
import io.cordacity.koncerto.contract.membership.MembershipAttestationState
import io.cordacity.koncerto.contract.membership.MembershipContract
import io.cordacity.koncerto.contract.membership.MembershipState
import io.cordacity.koncerto.contract.relationship.RelationshipAttestationContract
import io.cordacity.koncerto.contract.relationship.RelationshipAttestationState
import io.cordacity.koncerto.contract.relationship.RelationshipContract
import io.cordacity.koncerto.contract.relationship.RelationshipState
import io.cordacity.koncerto.contract.revocation.RevocationLockContract
import io.cordacity.koncerto.contract.revocation.RevocationLockState
import net.corda.core.contracts.StateAndRef
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.AbstractParty
import net.corda.core.node.NotaryInfo
import net.corda.testing.common.internal.testNetworkParameters
import net.corda.testing.contracts.DummyContract
import net.corda.testing.contracts.DummyState
import net.corda.testing.core.DUMMY_NOTARY_NAME
import net.corda.testing.core.TestIdentity
import net.corda.testing.dsl.LedgerDSL
import net.corda.testing.dsl.TestLedgerDSLInterpreter
import net.corda.testing.dsl.TestTransactionDSLInterpreter
import net.corda.testing.node.MockServices
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

private typealias DSL = LedgerDSL<TestTransactionDSLInterpreter, TestLedgerDSLInterpreter>

abstract class ContractTest : AutoCloseable {

    protected companion object {

        val cordapps = listOf(
            "io.cordacity.koncerto.contract.membership",
            "io.cordacity.koncerto.contract.relationship",
            "io.cordacity.koncerto.contract.revocation",
            "net.corda.testing.contracts"
        )

        val contracts = listOf(
            MembershipContract.ID,
            MembershipAttestationContract.ID,
            RelationshipContract.ID,
            RelationshipAttestationContract.ID,
            RevocationLockContract.ID,
            DummyContract.PROGRAM_ID
        )

        fun partiesOf(vararg identities: TestIdentity) = identities.map { it.party }
        fun keysOf(vararg identities: TestIdentity) = identities.map { it.publicKey }
    }

    private lateinit var _services: MockServices
    protected val services: MockServices get() = _services

    override fun close() = finalize()
    protected open fun initialize() = Unit
    protected open fun finalize() = Unit

    @BeforeEach
    private fun setup() {
        val networkParameters = testNetworkParameters(
            minimumPlatformVersion = 4,
            notaries = listOf(NotaryInfo(TestIdentity(DUMMY_NOTARY_NAME, 20).party, true))
        )
        _services = MockServices(cordapps, IDENTITY_A, networkParameters, IDENTITY_B, IDENTITY_C)
        contracts.forEach { _services.addMockCordapp(it) }
        initialize()
    }

    @AfterEach
    private fun tearDown() = close()

    protected fun DSL.createDummyOutput(
        label: String = SecureHash.randomSHA256().toString()
    ): StateAndRef<DummyState> {
        transaction {
            output(DummyContract.PROGRAM_ID, label, DummyState(participants = partiesOf(IDENTITY_A)))
            command(keysOf(IDENTITY_A), DummyContract.Commands.Create())
            verifies()
        }

        return retrieveOutputStateAndRef(DummyState::class.java, label)
    }

    @Suppress("UNCHECKED_CAST")
    protected fun DSL.initialize(
        membershipState: MembershipState<*>,
        label: String = SecureHash.randomSHA256().toString()
    ): Pair<StateAndRef<MembershipState<DummyIdentity>>, MembershipState<DummyIdentity>> {
        transaction {
            output(MembershipContract.ID, label, membershipState)
            command(listOf(membershipState.identity.networkIdentity.owningKey), MembershipContract.Issue)
            verifies()
        }

        val input = retrieveOutputStateAndRef(
            MembershipState::class.java,
            label
        ) as StateAndRef<MembershipState<DummyIdentity>>

        return Pair(input, input.getNextOutput())
    }

    @Suppress("UNCHECKED_CAST")
    protected fun DSL.initialize(
        relationshipState: RelationshipState<*>,
        label: String = SecureHash.randomSHA256().toString()
    ): Pair<StateAndRef<RelationshipState<DummyConfiguration>>, RelationshipState<DummyConfiguration>> {
        transaction {
            val keys = relationshipState.participants.map { it.owningKey }
            output(RelationshipContract.ID, label, relationshipState)
            relationshipState.participants.forEach {
                output(RevocationLockContract.ID, RevocationLockState.create(it, relationshipState))
            }
            command(keys, RevocationLockContract.Create)
            command(keys, RelationshipContract.Issue)
            verifies()
        }

        val input = retrieveOutputStateAndRef(
            RelationshipState::class.java,
            label
        ) as StateAndRef<RelationshipState<DummyConfiguration>>

        return Pair(input, input.getNextOutput())
    }

    protected fun DSL.initialize(
        membershipState: MembershipState<*>,
        attestor: AbstractParty,
        label: String = SecureHash.randomSHA256().toString()
    ): Pair<StateAndRef<MembershipState<*>>, MembershipAttestationState> {
        transaction {
            output(MembershipContract.ID, label, membershipState)
            command(listOf(membershipState.identity.networkIdentity.owningKey), MembershipContract.Issue)
            verifies()
        }

        val membership = retrieveOutputStateAndRef(MembershipState::class.java, label)
        val attestation = MembershipAttestationState.create(attestor, membership, AttestationStatus.ACCEPTED)
        return Pair(membership, attestation)
    }

    protected fun DSL.initialize(
        relationshipState: RelationshipState<*>,
        attestor: AbstractParty,
        label: String = SecureHash.randomSHA256().toString()
    ): Pair<StateAndRef<RelationshipState<*>>, RelationshipAttestationState> {
        transaction {
            output(RelationshipContract.ID, label, relationshipState)
            relationshipState.participants.forEach {
                output(RevocationLockContract.ID, RevocationLockState.create(it, relationshipState))
            }
            val keys = relationshipState.participants.map { it.owningKey }
            command(keys, RevocationLockContract.Create)
            command(keys, RelationshipContract.Issue)
            verifies()
        }

        val relationship = retrieveOutputStateAndRef(RelationshipState::class.java, label)
        val attestation = RelationshipAttestationState.create(attestor, relationship, AttestationStatus.ACCEPTED)
        return Pair(relationship, attestation)
    }
}