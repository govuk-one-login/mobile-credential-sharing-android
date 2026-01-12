package uk.gov.onelogin.sharing.bluetooth

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import uk.gov.onelogin.sharing.core.R.string.denied

/**
 * A Composable that requests the user to enable Bluetooth.
 *
 * This function launches `ACTION_REQUEST_ENABLE` and returns 'RESULT_OK' if the user grants
 * permission.
 */
@Composable
fun EnableBluetoothPrompt(
    modifier: Modifier = Modifier,
    denialText: Int = denied,
    onResult: (Boolean) -> Unit = {}
) {
    var showDeniedMessage by remember { mutableStateOf(false) }

    val resultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val enabled = result.resultCode == Activity.RESULT_OK
        if (!enabled) {
            showDeniedMessage = true
        }
        onResult(enabled)
    }

    LaunchedEffect(Unit) {
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        resultLauncher.launch(intent)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("EnableBluetoothPrompt")
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (showDeniedMessage) {
            Text(
                text = stringResource(denialText),
                textAlign = TextAlign.Center
            )
        }
    }
}
