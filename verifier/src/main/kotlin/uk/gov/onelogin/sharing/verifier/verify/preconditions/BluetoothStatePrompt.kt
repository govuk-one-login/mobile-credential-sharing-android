package uk.gov.onelogin.sharing.verifier.verify.preconditions

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

/**
 * Prompts the user to enable the Bluetooth setting for the device when `isBluetoothEnabled` is false
 */
@Composable
fun BluetoothStatePrompt(isBluetoothEnabled: Boolean, onSuccess: (Boolean) -> Unit) {
    val launcher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            onSuccess(
                result.resultCode == Activity.RESULT_OK
            )
        }

    LaunchedEffect(isBluetoothEnabled) {
        if (!isBluetoothEnabled) {
            launcher.launch(
                Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            )
        }
    }
}
