package uk.gov.onelogin.sharing.orchestration.prerequisites.authorization

/**
 * Sealed class to represent different types of requests sent to the
 * [uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteGate.Authorization] interface.
 */
sealed class AuthorizationRequest {
    /**
     * Requests that the provided [permissions] are granted on the Android-powered device.
     */
    data class AuthorizePermission(val permissions: List<String>) : AuthorizationRequest() {
        constructor(
            vararg permissions: String
        ) : this(permissions.asList())
    }
}
