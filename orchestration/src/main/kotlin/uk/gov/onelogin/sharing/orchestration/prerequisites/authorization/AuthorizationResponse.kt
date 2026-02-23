package uk.gov.onelogin.sharing.orchestration.prerequisites.authorization

/**
 * Sealed class to represent different types of responses returned by the
 * [uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteGate.Authorization] interface.
 */
sealed class AuthorizationResponse {
    /**
     * State for when the device passes the requested authorization.
     */
    data object Authorized : AuthorizationResponse()

    /**
     * State for when the device fails the requested authorization.
     */
    data class Unauthorized(val reason: UnauthorizedReason) : AuthorizationResponse()
}
