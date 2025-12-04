package uk.gov.onelogin.sharing.bluetooth.api.permissions

fun interface PermissionChecker {
    fun hasPermission(): Boolean
}
