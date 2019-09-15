package io.cordacity.koncerto.contract

import io.cordacity.koncerto.contract.DummyConfiguration.Companion.DECENTRALIZED_CONFIGURATION
import io.cordacity.koncerto.contract.HashUtils.createMembershipAttestationIdentifier
import io.cordacity.koncerto.contract.HashUtils.createMembershipIdentifier
import io.cordacity.koncerto.contract.HashUtils.createParticipantsHash
import io.cordacity.koncerto.contract.HashUtils.createRelationshipAttestationIdentifier
import io.cordacity.koncerto.contract.HashUtils.createRelationshipIdentifier
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

class HashUtilTests {

    @Test
    fun `createMembershipIdentifier should produce deterministic unique identifiers`() {

        // Arrange
        val identity = IDENTITY_A.party

        // Act
        val identifier1 = createMembershipIdentifier(Network("network"), identity, "123")
        val identifier2 = createMembershipIdentifier(Network("NETWORK"), identity, "456")

        // Assert
        assertEquals(identifier1, identifier2)
    }

    @Test
    fun `createMembershipAttestationIdentifier should produce deterministic unique identifiers`() {

        // Arrange
        val attestor = IDENTITY_A.party
        val attestee = IDENTITY_B.party
        val id = UUID.fromString("00000000-0000-4000-0000-000000000000")

        // Act
        val identifier1 = createMembershipAttestationIdentifier(Network("network"), attestor, attestee, id, "123")
        val identifier2 = createMembershipAttestationIdentifier(Network("NETWORK"), attestor, attestee, id, "456")

        // Assert
        assertEquals(identifier1, identifier2)
    }

    @Test
    fun `createRelationshipIdentifier should produce deterministic unique identifiers`() {

        // Arrange
        val configuration = DECENTRALIZED_CONFIGURATION

        // Act
        val identifier1 = createRelationshipIdentifier(Network("network"), configuration, "123")
        val identifier2 = createRelationshipIdentifier(Network("network"), configuration, "456")

        // Assert
        assertEquals(identifier1, identifier2)
    }

    @Test
    fun `createRelationshipAttestationIdentifier should produce deterministic unique identifiers`() {

        // Arrange
        val attestor = IDENTITY_A.party
        val id = UUID.fromString("00000000-0000-4000-0000-000000000000")

        // Act
        val identifier1 = createRelationshipAttestationIdentifier(Network("network"), attestor, id, "123")
        val identifier2 = createRelationshipAttestationIdentifier(Network("network"), attestor, id, "123")

        // Assert
        assertEquals(identifier1, identifier2)
    }

    @Test
    fun `createParticipantsHash should produce deterministic secure hashes for the same sets of participants`() {

        // Arrange
        val set1 = setOf(IDENTITY_A.party, IDENTITY_B.party, IDENTITY_C.party)
        val set2 = setOf(IDENTITY_C.party, IDENTITY_A.party, IDENTITY_B.party)

        // Act
        val hash1 = createParticipantsHash(set1)
        val hash2 = createParticipantsHash(set2)

        // Assert
        assertEquals(hash1, hash2)
    }
}