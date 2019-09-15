package io.cordacity.koncerto.workflow

import io.cordacity.koncerto.contract.Configuration
import io.cordacity.koncerto.contract.Identity
import net.corda.core.identity.AbstractParty
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
class DummyIdentity(override val networkIdentity: AbstractParty) : Identity()

@CordaSerializable
class DummyConfig(override val name: String, override val networkIdentities: Set<AbstractParty>) : Configuration()