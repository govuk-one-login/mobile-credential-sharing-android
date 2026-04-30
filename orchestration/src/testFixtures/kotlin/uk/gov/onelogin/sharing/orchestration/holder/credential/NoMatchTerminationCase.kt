package uk.gov.onelogin.sharing.orchestration.holder.credential

/**
 * Test parameter values for no-match termination scenarios.
 * Used by TestParameterInjector via reflection.
 */
@Suppress("unused")
enum class NoMatchTerminationCase(val errorMessage: String) {
    MSO_DECODE_FAILURE(CredentialRequestHandlerImpl.LOG_MSO_DECODE_ERROR),
    HOST_APP_ERROR(CredentialRequestHandlerImpl.LOG_GET_CREDENTIALS_ERROR),
    ZERO_CREDENTIALS(CredentialRequestHandlerImpl.LOG_NO_CREDENTIALS),
    DOCTYPE_MISMATCH(CredentialRequestHandlerImpl.LOG_DOCTYPE_MISMATCH)
}
