package uk.gov.onelogin.sharing.verifier.connect

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import uk.gov.android.ui.theme.spacingDouble
import uk.gov.onelogin.sharing.core.presentation.bluetooth.BluetoothSessionError
import uk.gov.onelogin.sharing.verifier.R
import uk.gov.onelogin.sharing.core.R as coreR

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
        ConnectWithHolderDeviceScreenContent(
            contentState = contentState,
            modifier = Modifier,
        )

        if (contentState.isLoading) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun ConnectWithHolderDeviceScreenContent(
    contentState: ConnectWithHolderDeviceState,
    modifier: Modifier = Modifier,
) {

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(spacingDouble)
    ) {
        showBluetoothDeviceState { contentState.isBluetoothEnabled }
    }
}

private fun LazyListScope.showBluetoothDeviceState(isEnabled: () -> Boolean) {
    item {
        val deviceBluetoothState = if (isEnabled()) {
            coreR.string.enabled
        } else {
            coreR.string.disabled
        }.let { stringResource(it) }

        Text(
            stringResource(
                R.string.connect_with_holder_bluetooth_state,
                deviceBluetoothState
            )
        )
    }
}
