package io.cordacity.koncerto.contract

import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.AbstractParty
import java.security.MessageDigest
import java.util.*

internal object HashUtils {

    fun createMembershipIdentifier(
        network: Network,
        networkIdentity: AbstractParty,
        externalId: String?
    ): UniqueIdentifier {
        val messageDigest = MessageDigest.getInstance("MD5")
        messageDigest.update("${network.hash}$networkIdentity".toByteArray())
        return UniqueIdentifier(externalId, UUID.nameUUIDFromBytes(messageDigest.digest()))
    }

    fun createMembershipAttestationIdentifier(
        network: Network,
        attestor: AbstractParty,
        attestee: AbstractParty,
        membershipId: UUID,
        externalId: String?
    ): UniqueIdentifier {
        val messageDigest = MessageDigest.getInstance("MD5")
        messageDigest.update("${network.hash}$attestor$attestee$membershipId".toByteArray())
        return UniqueIdentifier(externalId, UUID.nameUUIDFromBytes(messageDigest.digest()))
    }

    fun createRelationshipIdentifier(
        network: Network,
        configuration: Configuration,
        externalId: String?
    ): UniqueIdentifier {
        val messageDigest = MessageDigest.getInstance("MD5")
        messageDigest.update("${network.hash}${configuration.hash}".toByteArray())
        return UniqueIdentifier(externalId, UUID.nameUUIDFromBytes(messageDigest.digest()))
    }

    fun createRelationshipAttestationIdentifier(
        network: Network,
        attestor: AbstractParty,
        relationshipId: UUID,
        externalId: String?
    ): UniqueIdentifier {
        val messageDigest = MessageDigest.getInstance("MD5")
        messageDigest.update("${network.hash}$attestor$relationshipId".toByteArray())
        return UniqueIdentifier(externalId, UUID.nameUUIDFromBytes(messageDigest.digest()))
    }

    fun createParticipantsHash(participants: Set<AbstractParty>): SecureHash {
        return SecureHash.sha256(participants.toSortedSet(Comparator { p0, p1 ->
            (p0?.hashCode() ?: 0).compareTo(p1?.hashCode() ?: 0)
        }).joinToString())
    }
}