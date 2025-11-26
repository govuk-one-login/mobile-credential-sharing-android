package uk.gov.onelogin.sharing.bluetooth

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

enum class BluetoothStatus {
    BLUETOOTH_ON,
    BLUETOOTH_OFF,
    INITIALIZING
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BluetoothDialog(
    onStateChange: @Composable (BluetoothStatus) -> Unit
) {
    var bluetoothStatus by remember { mutableStateOf(BluetoothStatus.INITIALIZING) }

    val resultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            -1 -> {
                bluetoothStatus = BluetoothStatus.BLUETOOTH_ON
            }

            0 -> {
                bluetoothStatus = BluetoothStatus.BLUETOOTH_OFF
            }
        }
    }

    LaunchedEffect(Unit) {
        //  if (hasPermissions) {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        resultLauncher.launch(enableBtIntent)
        //    }
    }

    onStateChange(bluetoothStatus)
}
