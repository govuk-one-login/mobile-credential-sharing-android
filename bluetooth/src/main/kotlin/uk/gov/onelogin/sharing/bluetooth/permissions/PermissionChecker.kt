package uk.gov.onelogin.sharing.bluetooth.permissions

fun interface PermissionChecker {
    fun hasPermission(): Boolean
}
