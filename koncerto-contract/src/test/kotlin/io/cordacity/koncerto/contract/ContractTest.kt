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
import net.corda.core.identity.AbstractParty
import net.corda.core.node.NotaryInfo
import net.corda.testing.common.internal.testNetworkParameters
import net.corda.testing.core.DUMMY_NOTARY_NAME
import net.corda.testing.core.TestIdentity
import net.corda.testing.dsl.LedgerDSL
import net.corda.testing.dsl.TestLedgerDSLInterpreter
import net.corda.testing.dsl.TestTransactionDSLInterpreter
import net.corda.testing.node.MockServices
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

abstract class ContractTest : AutoCloseable {

    protected companion object {

        val cordapps = listOf(
            "io.cordacity.koncerto.contract.membership",
            "io.cordacity.koncerto.contract.relationship",
            "io.cordacity.koncerto.contract.revocation"
        )

        val contracts = listOf(
            MembershipContract.ID,
            MembershipAttestationContract.ID,
            RelationshipContract.ID,
            RelationshipAttestationContract.ID,
            RevocationLockContract.ID
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

    protected fun initialize(
        dsl: LedgerDSL<TestTransactionDSLInterpreter, TestLedgerDSLInterpreter>,
        membership: MembershipState<*>,
        attestor: AbstractParty
    ): Pair<StateAndRef<MembershipState<*>>, MembershipAttestationState> {
        dsl.transaction {
            output(MembershipContract.ID, "membership", membership)
            command(listOf(membership.identity.networkIdentity.owningKey), MembershipContract.Issue)
            verifies()
        }

        val recordedMembership = dsl.retrieveOutputStateAndRef(MembershipState::class.java, "membership")
        val attestation = MembershipAttestationState
            .create(attestor, recordedMembership, AttestationStatus.ACCEPTED)
        return Pair(recordedMembership, attestation)
    }

    protected fun initialize(
        dsl: LedgerDSL<TestTransactionDSLInterpreter, TestLedgerDSLInterpreter>,
        relationship: RelationshipState<*>
    ): Pair<StateAndRef<RelationshipState<*>>, RelationshipAttestationState> {
        dsl.transaction {
            output(RelationshipContract.ID, "relationship", relationship)
            relationship.participants.forEach {
                output(RevocationLockContract.ID, RevocationLockState.create(it, relationship))
            }
            val keys = relationship.participants.map { it.owningKey }
            command(keys, RevocationLockContract.Create)
            command(keys, RelationshipContract.Issue)
            verifies()
        }

        val recordedRelationship = dsl.retrieveOutputStateAndRef(RelationshipState::class.java, "relationship")
        val attestation = RelationshipAttestationState
            .create(IDENTITY_A.party, recordedRelationship, AttestationStatus.ACCEPTED)
        return Pair(recordedRelationship, attestation)
    }
}