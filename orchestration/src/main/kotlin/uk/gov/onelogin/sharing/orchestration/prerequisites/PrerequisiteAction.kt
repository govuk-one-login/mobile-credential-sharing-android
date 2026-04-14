package uk.gov.onelogin.sharing.orchestration.prerequisites

import android.bluetooth.BluetoothAdapter
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions.Companion.ACTION_REQUEST_PERMISSIONS

sealed class PrerequisiteAction(val intentAction: String) {
    data class RequestPermissions(val permissions: List<String>) :
        PrerequisiteAction(
            ACTION_REQUEST_PERMISSIONS
        ) {
        constructor(
            vararg permissions: String
        ) : this(permissions.toList())

        operator fun plus(action: RequestPermissions): RequestPermissions =
            RequestPermissions(this.permissions + action.permissions)
    }

    data object OpenAppPermissions : PrerequisiteAction(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS
    )

    data object EnableBluetooth : PrerequisiteAction(
        BluetoothAdapter.ACTION_REQUEST_ENABLE
    )

    // Requires Google Play Services SettingsClient
    data object EnableLocationServices : PrerequisiteAction(
        Settings.ACTION_LOCATION_SOURCE_SETTINGS
    )
}
