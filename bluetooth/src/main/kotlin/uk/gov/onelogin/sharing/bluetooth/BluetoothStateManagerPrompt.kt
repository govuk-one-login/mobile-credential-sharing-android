package uk.gov.onelogin.sharing.bluetooth

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.accompanist.permissions.ExperimentalPermissionsApi

/**
 * A Composable that manages the state of a Bluetooth enable request.
 *
 * This function launches `ACTION_REQUEST_ENABLE` and returns 'RESULT_OK' if the user grants
 * permission
 *
 * The state is exposed via the [onStateChange] callBack
 *
 * @param onStateChange A composable lambda that is invoked with the current [BluetoothStatus].
 *
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BluetoothStateManagerPrompt(onStateChange: @Composable (BluetoothStatus) -> Unit) {
    var bluetoothStatus by remember { mutableStateOf(BluetoothStatus.INITIALIZING) }

    val resultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        bluetoothStatus = when (result.resultCode) {
            Activity.RESULT_OK -> BluetoothStatus.BLUETOOTH_ON
            else -> BluetoothStatus.BLUETOOTH_OFF
        }
    }

    LaunchedEffect(Unit) {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        resultLauncher.launch(enableBtIntent)
    }

    onStateChange(bluetoothStatus)
}

enum class BluetoothStatus {
    BLUETOOTH_ON,
    BLUETOOTH_OFF,
    INITIALIZING
}
