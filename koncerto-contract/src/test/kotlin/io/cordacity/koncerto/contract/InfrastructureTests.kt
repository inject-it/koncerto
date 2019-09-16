package io.cordacity.koncerto.contract

import net.corda.core.crypto.NullKeys.NULL_PARTY
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class InfrastructureTests {

    @Test
    fun `Roles should be equal by normalizedName`() {

        // Arrange
        val roles = setOf(
            Role("USER"),
            Role("user"),
            Role("User")
        )

        // Act
        // No action to take

        // Assert
        assertEquals(1, roles.size)
        assertEquals("user", roles.single().normalizedName)
    }

    @Test
    fun `Claims should be equal by normalizedKey and normalizedValue`() {

        // Arrange
        val claims = setOf(
            Claim("KEY", "VALUE"),
            Claim("key", "value"),
            Claim("Key", "Value")
        )

        // Act
        // No action to take

        // Assert
        assertEquals(1, claims.size)
        assertEquals("key", claims.single().normalizedKey)
        assertEquals("value", claims.single().normalizedValue)
    }

    @Test
    fun `Networks are considered equal by normalizedName and hash (centralized network)`() {

        // Arrange
        val networks = setOf(
            Network("DECENTRALIZED NETWORK", OPERATOR_A.party),
            Network("decentralized network", OPERATOR_A.party),
            Network("Decentralized Network", OPERATOR_A.party)
        )

        // Act
        // No action to take

        // Assert
        assertEquals(1, networks.size)
        assertEquals("decentralized network", networks.single().normalizedName)
        assertEquals(
            "F07EF9CDBF92062BC8676586D731E7DBB1942E189F403D2838E8EF59266C6737",
            networks.single().hash.toString()
        )
    }

    @Test
    fun `Networks are considered equal by normalizedName and hash (decentralized network)`() {

        // Arrange
        val networks = setOf(
            Network("DECENTRALIZED NETWORK"),
            Network("decentralized network"),
            Network("Decentralized Network")
        )

        // Act
        // No action to take

        // Assert
        assertEquals(1, networks.size)
        assertEquals("decentralized network", networks.single().normalizedName)
        assertEquals(
            "83C028F8C5797341B3053B0F46712DF48753C3CEAB267F1299A7B193401B4403",
            networks.single().hash.toString()
        )
    }

    @Test
    fun `Configurations are considered equal by normalizedName and hash`() {

        // Arrange
        val configurations = setOf(
            DummyConfiguration("CONFIGURATION", setOf(NULL_PARTY)),
            DummyConfiguration("configuration", setOf(NULL_PARTY)),
            DummyConfiguration("Configuration", setOf(NULL_PARTY))
        )

        // Act
        // No action to take

        // Assert
        assertEquals(1, configurations.size)
        assertEquals("configuration", configurations.single().normalizedName)
        assertEquals(
            "5545045A8E37B6960DE7B9556C4C89B0A4987CC04535CB9DB8ADC3F641C56AFA",
            configurations.single().hash.toString()
        )
    }
}