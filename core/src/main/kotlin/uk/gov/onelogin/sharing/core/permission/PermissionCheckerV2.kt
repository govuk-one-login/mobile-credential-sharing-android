package uk.gov.onelogin.sharing.core.permission

fun interface PermissionCheckerV2 {
    /**
     * @return An empty [List] when all requested [permissions] are granted. Otherwise, a list of
     * [Denied] objects
     */
    fun checkPermissions(permissions: List<String>): List<Denied>

    /**
     * @return An empty [List] when all requested [permissions] are granted. Otherwise, a list of
     * [Denied] objects
     */
    fun checkPermissions(vararg permissions: String): List<Denied> = checkPermissions(
        permissions.asList()
    )

    data class Denied(val permission: String, val shouldShowRationale: Boolean)
}
