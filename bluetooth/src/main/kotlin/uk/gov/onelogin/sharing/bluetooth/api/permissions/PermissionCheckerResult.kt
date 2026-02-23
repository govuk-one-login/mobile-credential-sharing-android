package uk.gov.onelogin.sharing.bluetooth.api.permissions

/**
 * State table for verifying granted permissions on an Android-powered device.
 */
sealed class PermissionCheckerResult {
    /**
     * State for when all associated permissions are currently granted on the Android-powered
     * device.
     */
    data object Passed : PermissionCheckerResult()

    /**
     * State for when there are required permissions that're currently denied on the Android-powered
     * device.
     *
     * @param missingPermissions The list of [android.Manifest.permission] permissions that need granting
     * by the User. Defaults to an empty list.
     *
     * @sample uk.gov.onelogin.sharing.bluetooth.internal.permissions.bluetooth.Api31BluetoothPermissionChecker.checkPeripheralPermissions
     */
    data class Missing(
        val missingPermissions: List<String> = emptyList()
    ) : PermissionCheckerResult()
}