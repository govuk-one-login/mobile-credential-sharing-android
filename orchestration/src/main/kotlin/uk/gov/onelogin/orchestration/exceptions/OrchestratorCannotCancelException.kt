package uk.gov.onelogin.orchestration.exceptions

class OrchestratorCannotCancelException(
    override val message: String,
    override val cause: Throwable
) : Exception(message, cause)
