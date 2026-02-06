package uk.gov.onelogin.orchestration.exceptions

data class OrchestratorCannotStartException(
    override val message: String,
    override val cause: Throwable
) : Exception(message, cause)
