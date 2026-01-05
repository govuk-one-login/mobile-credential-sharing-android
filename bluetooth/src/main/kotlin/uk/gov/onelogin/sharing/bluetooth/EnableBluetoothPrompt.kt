package uk.gov.onelogin.sharing.bluetooth

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

/**
 * A Composable that requests the user to enable Bluetooth.
 *
 * This function launches `ACTION_REQUEST_ENABLE` and returns 'RESULT_OK' if the user grants
 * permission.
 */
@Composable
fun EnableBluetoothPrompt(onResult: (Boolean) -> Unit = {}) {
    val resultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val enabled = result.resultCode == Activity.RESULT_OK
        onResult(enabled)
    }

    LaunchedEffect(Unit) {
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        resultLauncher.launch(intent)
    }
}
