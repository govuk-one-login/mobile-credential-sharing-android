package uk.gov.onelogin.sharing.holder.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.util.UUID
import uk.gov.onelogin.sharing.bluetooth.api.MdocSessionState
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
            contentState = contentState
        )
    }

    LaunchedEffect(Unit) {
        viewModel.startAdvertising()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopAdvertising()
        }
    }
}

// This will be updated in - https://govukverify.atlassian.net/browse/DCMAW-16531
@Composable
private fun RequestPermissions() {
    val context = LocalContext.current

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val requiredPermissions = arrayOf(
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT
        )

        var allPermissionsGranted by remember {
            mutableStateOf(
                requiredPermissions.all { permission ->
                    ContextCompat.checkSelfPermission(
                        context,
                        permission
                    ) == PackageManager.PERMISSION_GRANTED
                }
            )
        }

        val permissionsLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
            onResult = { permissions ->
                allPermissionsGranted = permissions.values.all { it }
            }
        )

        LaunchedEffect(allPermissionsGranted) {
            if (!allPermissionsGranted) {
                permissionsLauncher.launch(requiredPermissions)
            }
        }
    }
}

@Suppress("LongMethod")
@Composable
fun HolderWelcomeScreenContent(contentState: HolderWelcomeUiState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        contentState.qrData?.let {
            QrCodeImage(
                data = it,
                size = QR_SIZE
            )
        }
    }
}

@Preview
@Composable
private fun HolderWelcomeScreenPreview() {
    val contentState = HolderWelcomeUiState(
        lastErrorMessage = null,
        sessionState = MdocSessionState.Advertising,
        uuid = UUID.randomUUID(),
        qrData = "QR Data"
    )

    HolderWelcomeScreenContent(
        contentState = contentState,
        modifier = Modifier
    )
}
