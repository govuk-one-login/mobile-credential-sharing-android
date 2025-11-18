package uk.gov.onelogin.sharing.holder.presentation

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.util.UUID
import uk.gov.onelogin.sharing.bluetooth.api.AdvertiserState
import uk.gov.onelogin.sharing.bluetooth.internal.permissions.BluetoothPermissionChecker
import uk.gov.onelogin.sharing.holder.QrCodeImage

private const val QR_SIZE = 800

@Composable
fun HolderWelcomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HolderWelcomeViewModel = HolderWelcomeViewModel.holderWelcomeViewModel()
) {
    val contentState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier) {
        RequestPermissions()
        HolderWelcomeText()

        HolderWelcomeScreenContent(
            contentState = contentState,
            onStartClick = viewModel::onStartAdvertise,
            onStopClick = viewModel::onStopAdvertise,
            onShowError = viewModel::onErrorMessageShown
        )
    }
}

// This will be updated in - https://govukverify.atlassian.net/browse/DCMAW-16531
@Composable
private fun RequestPermissions() {
    val context = LocalContext.current
    val permissionChecker = BluetoothPermissionChecker(context)
    var hasPermission by remember { mutableStateOf(permissionChecker.hasPermission()) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasPermission = isGranted
        }
    )

    LaunchedEffect(hasPermission) {
        if (!hasPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionLauncher.launch(Manifest.permission.BLUETOOTH_ADVERTISE)
        }
    }
}

@Suppress("LongMethod")
@Composable
fun HolderWelcomeScreenContent(
    contentState: HolderWelcomeUiState,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit,
    onShowError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val currentErrorShown by rememberUpdatedState(onShowError)

    LaunchedEffect(contentState.lastErrorMessage) {
        if (contentState.lastErrorMessage != null) {
            Toast.makeText(
                context,
                contentState.lastErrorMessage,
                Toast.LENGTH_LONG
            ).show()
            currentErrorShown()
        }
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        contentState.qrData?.let {
            QrCodeImage(
                data = it,
                size = QR_SIZE
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Bluetooth Advertising",
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Row {
            Button(onClick = onStartClick) {
                Text("Start")
            }

            Spacer(modifier = Modifier.padding(20.dp))

            Button(
                enabled = contentState.advertiserState == AdvertiserState.Started,
                onClick = onStopClick
            ) {
                Text("Stop")
            }
        }

        Text(
            text = "Status: ${contentState.advertiserState}",
            modifier = Modifier.padding(vertical = 16.dp)
        )

        if (contentState.advertiserState == AdvertiserState.Started) {
            Text(
                text = "UUID: ${contentState.uuid}"
            )
        }
    }
}

@Preview
@Composable
private fun HolderWelcomeScreenPreview() {
    val contentState = HolderWelcomeUiState(
        lastErrorMessage = null,
        advertiserState = AdvertiserState.Started,
        uuid = UUID.randomUUID(),
        qrData = "QR Data"
    )

    HolderWelcomeScreenContent(
        contentState = contentState,
        onStartClick = {},
        onStopClick = {},
        onShowError = {},
        modifier = Modifier
    )
}
