package uk.gov.onelogin.orchestration

import uk.gov.onelogin.sharing.core.Resettable

/**
 * Implements [Resettable] for clearing internal state, such as the session state machines.
 */
interface Orchestrator : Resettable {

    fun start(requiredPermissions: Set<String>)

    fun cancel()

    interface Holder : Orchestrator
    interface Verifier : Orchestrator

    /**
     * Property bag object containing logging messages common to [Orchestrator] implementations.
     */
    data object LogMessages {
        const val CANCEL_ORCHESTRATION_ERROR: String = "Cannot cancel orchestration"
        const val CANCEL_ORCHESTRATION_SUCCESS: String = "cancel orchestration"
        const val START_ORCHESTRATION_ERROR: String = "Cannot start orchestration"
        const val START_ORCHESTRATION_SUCCESS: String = "start orchestration"
    }
}
