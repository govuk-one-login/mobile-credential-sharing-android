package uk.gov.onelogin.orchestration.exceptions

data class OrchestratorCannotStartException(
    override val message: String,
    override val cause: Throwable
) : Exception(message, cause) {
    companion object {
        private const val serialVersionUID: Long = -8668578562716053514L
    }
}
