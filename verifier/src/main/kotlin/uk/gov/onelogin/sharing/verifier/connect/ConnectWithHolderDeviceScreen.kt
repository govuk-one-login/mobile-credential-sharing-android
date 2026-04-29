package uk.gov.onelogin.sharing.verifier.connect

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import uk.gov.onelogin.sharing.core.presentation.bluetooth.BluetoothSessionError

@Composable
fun ConnectWithHolderDeviceScreen(
    modifier: Modifier = Modifier,
    viewModel: SessionEstablishmentViewModel = metroViewModel(),
    onConnectionError: (BluetoothSessionError) -> Unit = {}
) {
    val latestOnConnectionError by rememberUpdatedState(onConnectionError)

    val contentState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.navEvents.collect {
            when (it) {
                is ConnectWithHolderDeviceNavEvent.NavigateToError ->
                    latestOnConnectionError(it.error)
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (contentState.isLoading) {
            CircularProgressIndicator()
        } else {
            Text("Connect with holder device screen")
        }
    }
}
