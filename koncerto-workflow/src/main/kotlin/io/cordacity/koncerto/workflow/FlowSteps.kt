package io.cordacity.koncerto.workflow

import io.cordacity.koncerto.workflow.common.CheckedReceiveFinalityFlow
import net.corda.core.flows.CollectSignaturesFlow
import net.corda.core.flows.FinalityFlow
import net.corda.core.utilities.ProgressTracker.Step

internal object QUERYING : Step("Performing vault query.")

internal object INITIALIZING : Step("Initializing flow.")

internal object GENERATING : Step("Generating initial transaction.")

internal object VERIFYING : Step("Verifying initial transaction.")

internal object SIGNING : Step("Signing initial transaction.")

internal object COUNTERSIGNING : Step("Checking and counter-signing transaction.")

internal object GATHERING : Step("Gathering counter-party signatures.") {
    override fun childProgressTracker() = CollectSignaturesFlow.tracker()
}

internal object FINALIZING : Step("Finalizing signed transaction.") {
    override fun childProgressTracker() = FinalityFlow.tracker()
}

internal object CHECKED_FINALIZING : Step("Checking and finalizing signed transaction.") {
    override fun childProgressTracker() = CheckedReceiveFinalityFlow.tracker()
}