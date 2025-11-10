package uk.gov.onelogin.sharing.bluetooth.permissions

interface PermissionChecker {
    fun hasPermission(): Boolean
}
