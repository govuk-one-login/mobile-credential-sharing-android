package uk.gov.onelogin.orchestration.exceptions

data class OrchestratorCannotCancelException(
    override val message: String,
    override val cause: Throwable
) : Exception(message, cause) {
    companion object {
        private const val serialVersionUID: Long = -4676991796727656656L
    }
}
