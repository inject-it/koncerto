package io.cordacity.koncerto.workflow

import io.cordacity.koncerto.contract.Configuration
import io.cordacity.koncerto.contract.Identity
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class DummyIdentity(override val networkIdentity: Party) : Identity()

@CordaSerializable
class DummyConfig(override val name: String, override val networkIdentities: Set<AbstractParty>) : Configuration()

data class DummyLinearState(
    override val linearId: UniqueIdentifier = UniqueIdentifier(),
    override val participants: List<AbstractParty> = emptyList()
) : LinearState