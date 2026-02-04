package uk.gov.onelogin.orchestration

sealed interface OrchestratorEvent {

    sealed interface Holder : OrchestratorEvent {
        data object Start : Holder
        data object Cancel : Holder
    }
}
