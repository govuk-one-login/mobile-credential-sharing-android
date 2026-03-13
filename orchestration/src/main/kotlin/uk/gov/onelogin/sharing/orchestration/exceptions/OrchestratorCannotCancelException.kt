package uk.gov.onelogin.sharing.orchestration.exceptions

class OrchestratorCannotCancelException(
    override val message: String,
    override val cause: Throwable
) : Exception(message, cause)
