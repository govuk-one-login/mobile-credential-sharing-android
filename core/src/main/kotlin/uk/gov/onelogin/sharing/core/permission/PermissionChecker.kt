package uk.gov.onelogin.sharing.core.permission

fun interface PermissionChecker {
    fun checkPermissions(permissions: List<String>): Response
    fun checkPermissions(vararg permissions: String): Response = checkPermissions(
        permissions.asList()
    )

    /**
     * State table for verifying granted permissions on an Android-powered device.
     */
    sealed class Response {
        /**
         * State for when all associated permissions are currently granted on the Android-powered
         * device.
         */
        data object Passed : Response()

        /**
         * State for when there are required permissions that're currently denied on the Android-powered
         * device.
         *
         * @param missingPermissions The list of [android.Manifest.permission] permissions that need granting
         * by the User. Defaults to an empty list.
         */
        data class Missing(val missingPermissions: List<String> = emptyList()) :
            Response(),
            Iterable<String> by missingPermissions {
            constructor(
                vararg permissions: String
            ) : this(
                permissions.asList()
            )
        }
    }
}
