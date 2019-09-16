package io.cordacity.koncerto.contract

import io.cordacity.koncerto.contract.DummyConfiguration.Companion.CENTRALIZED_CONFIGURATION
import io.cordacity.koncerto.contract.DummyConfiguration.Companion.DECENTRALIZED_CONFIGURATION
import io.cordacity.koncerto.contract.DummyIdentity.Companion.DUMMY_IDENTITY_A
import io.cordacity.koncerto.contract.DummyIdentity.Companion.DUMMY_IDENTITY_B
import io.cordacity.koncerto.contract.DummyIdentity.Companion.DUMMY_IDENTITY_C
import io.cordacity.koncerto.contract.DummyIdentity.Companion.DUMMY_OPERATOR_A
import io.cordacity.koncerto.contract.membership.MembershipState
import io.cordacity.koncerto.contract.relationship.RelationshipState
import io.cordacity.koncerto.contract.revocation.RevocationLockState
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import net.corda.testing.core.TestIdentity

@CordaSerializable
class DummyIdentity(
    override val networkIdentity: Party
) : Identity() {
    companion object {
        val DUMMY_IDENTITY_A = DummyIdentity(IDENTITY_A.party)
        val DUMMY_IDENTITY_B = DummyIdentity(IDENTITY_B.party)
        val DUMMY_IDENTITY_C = DummyIdentity(IDENTITY_C.party)
        val DUMMY_OPERATOR_A = DummyIdentity(OPERATOR_A.party)
    }
}

@CordaSerializable
class DummyConfiguration(
    override val name: String,
    override val networkIdentities: Set<AbstractParty>
) : Configuration() {
    companion object {
        val DECENTRALIZED_CONFIGURATION = DummyConfiguration("Decentralized", DECENTRALIZED_PARTICIPANTS)
        val CENTRALIZED_CONFIGURATION = DummyConfiguration("Centralized A", CENTRALIZED_PARTICIPANTS)
    }
}

val IDENTITY_A = TestIdentity(CordaX500Name("PartyA", "London", "GB"))
val IDENTITY_B = TestIdentity(CordaX500Name("PartyB", "New York", "US"))
val IDENTITY_C = TestIdentity(CordaX500Name("PartyC", "Paris", "FR"))
val OPERATOR_A = TestIdentity(CordaX500Name("OperatorA", "London", "GB"))

val DECENTRALIZED_PARTICIPANTS = setOf(IDENTITY_A.party, IDENTITY_B.party, IDENTITY_C.party)
val CENTRALIZED_PARTICIPANTS = setOf(IDENTITY_A.party, IDENTITY_B.party, IDENTITY_C.party, OPERATOR_A.party)

val DECENTRALIZED_NETWORK = Network("Decentralized Network")
val CENTRALIZED_NETWORK_A = Network("Centralized Network A", OPERATOR_A.party)
val INVALID_NETWORK = Network("Invalid Network")

val DECENTRALIZED_MEMBERSHIP_A = MembershipState(DECENTRALIZED_NETWORK, DUMMY_IDENTITY_A)
val DECENTRALIZED_MEMBERSHIP_B = MembershipState(DECENTRALIZED_NETWORK, DUMMY_IDENTITY_B)
val DECENTRALIZED_MEMBERSHIP_C = MembershipState(DECENTRALIZED_NETWORK, DUMMY_IDENTITY_C)

val CENTRALIZED_MEMBERSHIP_A = MembershipState(CENTRALIZED_NETWORK_A, DUMMY_IDENTITY_A)
val CENTRALIZED_MEMBERSHIP_B = MembershipState(CENTRALIZED_NETWORK_A, DUMMY_IDENTITY_B)
val CENTRALIZED_MEMBERSHIP_C = MembershipState(CENTRALIZED_NETWORK_A, DUMMY_IDENTITY_C)
val CENTRALIZED_MEMBERSHIP_O = MembershipState(CENTRALIZED_NETWORK_A, DUMMY_OPERATOR_A, setOf(Role.NETWORK_OPERATOR))

val CENTRALIZED_RELATIONSHIP = RelationshipState(CENTRALIZED_NETWORK_A, CENTRALIZED_CONFIGURATION)
val DECENTRALIZED_RELATIONSHIP = RelationshipState(DECENTRALIZED_NETWORK, DECENTRALIZED_CONFIGURATION)

val REVOCATION_LOCK = RevocationLockState.create(IDENTITY_A.party, DECENTRALIZED_RELATIONSHIP)