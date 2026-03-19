package uk.gov.onelogin.sharing.verifier.connect

import com.google.accompanist.permissions.ExperimentalPermissionsApi
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.DeviceEngagementDto

/**
 * Property bag data class for holding the [ConnectWithHolderDeviceScreen] composable UI state.
 *
 * @param base64EncodedEngagement The CBOR string that's embedded within a valid digital credential
 * QR code.
 * @param showErrorScreen Used for navigating away from [ConnectWithHolderDeviceScreen] when the
 * parameter isn't null. Defaults to null, meaning that the User should be shown
 * [ConnectWithHolderDeviceScreen].
 */
@OptIn(ExperimentalPermissionsApi::class)
data class ConnectWithHolderDeviceState(
    val isBluetoothEnabled: Boolean = false,
    val base64EncodedEngagement: String? = null,
    val engagementData: DeviceEngagementDto? = null,
    val hasAllPermissions: Boolean = false,
    val hasRequestedPermissions: Boolean = false,
    val connectionStateStarted: Boolean = false,
    val previouslyHadPermissions: Boolean = false
)
