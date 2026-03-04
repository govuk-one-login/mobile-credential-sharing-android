package uk.gov.onelogin.sharing.orchestration.prerequisites.authorization

/**
 * State table for the different reasons that a device fails to authorize.
 */
sealed class UnauthorizedReason {
    /**
     * State for when the User hasn't granted the requested permissions.
     *
     * @param missingPermissions The list of permissions that the User needs to grant.
     */
    data class MissingPermissions(val missingPermissions: Set<String>) :
        UnauthorizedReason(),
        Iterable<String> by missingPermissions {
        constructor(
            vararg permissions: String
        ) : this (permissions.toSet())
    }
}
