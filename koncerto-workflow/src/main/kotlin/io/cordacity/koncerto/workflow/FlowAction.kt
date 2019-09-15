package io.cordacity.koncerto.workflow

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
internal enum class FlowAction { ISSUING, AMENDING, REVOKING }