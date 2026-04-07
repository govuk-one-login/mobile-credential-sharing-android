package uk.gov.onelogin.sharing.orchestration.prerequisites

import android.bluetooth.BluetoothAdapter
import android.provider.Settings

sealed class PrerequisiteAction {
    data class RequestPermissions(val permissions: List<String>) : PrerequisiteAction() {
        constructor(
            vararg permissions: String
        ) : this(permissions.toList())
    }

    abstract class IntentHandoff(val intentData: String) : PrerequisiteAction()
    data object OpenAppPermissions : IntentHandoff(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    data object EnableBluetooth : IntentHandoff(BluetoothAdapter.ACTION_REQUEST_ENABLE)

    // Requires Google Play Services SettingsClient
    data object EnableLocationServices : IntentHandoff(
        Settings.ACTION_LOCATION_SOURCE_SETTINGS
    )
}
