package uk.gov.onelogin.sharing.verifier.connect

import com.google.accompanist.permissions.ExperimentalPermissionsApi

/**
 * Property bag data class for holding the [ConnectWithHolderDeviceScreen] composable UI state.
 */
@OptIn(ExperimentalPermissionsApi::class)
data class ConnectWithHolderDeviceState(
    val isLoading: Boolean = false
)
