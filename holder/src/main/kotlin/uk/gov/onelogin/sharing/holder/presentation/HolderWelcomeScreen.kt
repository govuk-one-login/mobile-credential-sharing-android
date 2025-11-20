package uk.gov.onelogin.sharing.holder.presentation

import android.Manifest
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.util.UUID
import uk.gov.onelogin.sharing.bluetooth.api.AdvertiserState
import uk.gov.onelogin.sharing.bluetooth.api.permissions.BluetoothPermissionChecker
import uk.gov.onelogin.sharing.holder.QrCodeImage

private const val QR_SIZE = 800

@Composable
fun HolderWelcomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HolderWelcomeViewModel = HolderWelcomeViewModel.holderWelcomeViewModel()
) {
    val contentState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.startAdvertising()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopAdvertising()
        }
    }

    Column(modifier = modifier) {
        HolderWelcomeText()

        HolderWelcomeScreenContent(
            contentState = contentState
        )
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
        advertiserState = AdvertiserState.Started,
        uuid = UUID.randomUUID(),
        qrData = "QR Data"
    )

    HolderWelcomeScreenContent(
        contentState = contentState,
        modifier = Modifier
    )
}
