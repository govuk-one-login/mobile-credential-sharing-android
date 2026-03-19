package uk.gov.onelogin.sharing.orchestration

data object OrchestratorStubs {
    /**
     * Duplicate of [Orchestrator.LogMessages] for testing
     * purposes.
     *
     * Ensures that unit tests fail when production logging messages update without changing
     * these properties.
     */
    data object LogMessages {
        const val START_ORCHESTRATION_ERROR: String = "Cannot start orchestration"
        const val START_ORCHESTRATION_SUCCESS: String = "start orchestration"
    }
}
