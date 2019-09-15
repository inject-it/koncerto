package io.cordacity.koncerto.contract

import io.cordacity.koncerto.contract.DummyConfiguration.Companion.CENTRALIZED_CONFIGURATION
import io.cordacity.koncerto.contract.DummyIdentity.Companion.DUMMY_IDENTITY_A
import io.cordacity.koncerto.contract.membership.MembershipState
import io.cordacity.koncerto.contract.relationship.RelationshipState
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class UniquenessTests {

    @Test
    fun `Identical Role instances produce the same hash`() {

        // Arrange
        val a = Role("USER")
        val b = Role("user")

        // Assert
        assertEquals(a.hash, b.hash)
    }

    @Test
    fun `Identical Claim instances produce the same hash`() {

        // Arrange
        val a = Claim("KEY", "VALUE")
        val b = Claim("key", "value")

        // Assert
        assertEquals(a.hash, b.hash)
    }

    @Test
    fun `Identical Network instances produce the same hash`() {

        // Arrange
        val a = Network("Test Network")
        val b = Network("test network")

        // Assert
        assertEquals(a.hash, b.hash)
    }

    @Test
    fun `Identical MembershipState instances produce the same linear ID`() {

        // Arrange
        val a = MembershipState(CENTRALIZED_NETWORK_A, DUMMY_IDENTITY_A)
        val b = MembershipState(CENTRALIZED_NETWORK_A, DUMMY_IDENTITY_A)

        // Assert
        assertEquals(a.linearId, b.linearId)
    }

    @Test
    fun `Identical RelationshipState instances produce the same linear ID`() {

        // Arrange
        val a = RelationshipState(CENTRALIZED_NETWORK_A, CENTRALIZED_CONFIGURATION)
        val b = RelationshipState(CENTRALIZED_NETWORK_A, CENTRALIZED_CONFIGURATION)

        // Assert
        assertEquals(a.linearId, b.linearId)
    }
}