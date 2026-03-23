package uk.gov.onelogin.sharing.verifier.connect

import com.google.accompanist.permissions.ExperimentalPermissionsApi

/**
 * Property bag data class for holding the [ConnectWithHolderDeviceScreen] composable UI state.
 */
@OptIn(ExperimentalPermissionsApi::class)
data class ConnectWithHolderDeviceState(
    val isBluetoothEnabled: Boolean = false,
    val hasAllPermissions: Boolean = false,
    val hasRequestedPermissions: Boolean = false,
    val connectionStateStarted: Boolean = false,
    val previouslyHadPermissions: Boolean = false
)
