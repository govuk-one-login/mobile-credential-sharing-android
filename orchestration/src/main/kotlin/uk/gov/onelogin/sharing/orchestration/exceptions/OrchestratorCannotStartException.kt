package uk.gov.onelogin.sharing.orchestration.exceptions

data class OrchestratorCannotStartException(
    override val message: String,
    override val cause: Throwable
) : Exception(message, cause)
