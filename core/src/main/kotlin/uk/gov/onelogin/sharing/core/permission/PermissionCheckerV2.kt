package uk.gov.onelogin.sharing.core.permission

fun interface PermissionCheckerV2 {
    /**
     * @return An empty [List] when all requested [permissions] are granted. Otherwise, a list of
     * [PermissionCheckResult] objects
     */
    fun checkPermissions(permissions: List<String>): List<PermissionCheckResult>

    /**
     * @return An empty [List] when all requested [permissions] are granted. Otherwise, a list of
     * [PermissionCheckResult] objects
     */
    fun checkPermissions(vararg permissions: String): List<PermissionCheckResult> =
        checkPermissions(
            permissions.asList()
        )

    /**
     * State table representing the results obtained from [PermissionCheckerV2.checkPermissions].
     *
     * @param permission The android permission [String] causing the failure.
     */
    sealed class PermissionCheckResult(open val permission: String) {
        /**
         * State for when a checked permission hasn't been requested by the app.
         *
         * Consumers should request the [permission] as part of standard permission flows.
         */
        data class Undetermined(override val permission: String) : PermissionCheckResult(permission)

        /**
         * State for when a checked permission has been requested and refused by the User.
         *
         * Consumers should request the [permission] with additional context so that the User makes
         * a more informed choice.
         */
        data class Denied(override val permission: String) : PermissionCheckResult(permission)

        /**
         * State for when a checked permission has been requested and refused by the User on
         * multiple occasions.
         *
         * To resolve, the User should be directed to the app's settings page to manually update
         * permissions.
         */
        data class PermanentlyDenied(override val permission: String) :
            PermissionCheckResult(permission)
    }
}
