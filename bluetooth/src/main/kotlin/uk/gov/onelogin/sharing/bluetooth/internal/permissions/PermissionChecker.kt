package uk.gov.onelogin.sharing.bluetooth.internal.permissions

// This will be updated in - https://govukverify.atlassian.net/browse/DCMAW-16531
fun interface PermissionChecker {
    fun hasPermission(): Boolean
}
