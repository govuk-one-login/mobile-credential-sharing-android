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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dev.zacsweers.metrox.viewmodel.metroViewModel
import uk.gov.android.ui.theme.spacingDouble
import uk.gov.onelogin.sharing.bluetooth.EnableBluetoothPrompt
import uk.gov.onelogin.sharing.bluetooth.api.permissions.bluetooth.BluetoothPermissionChecker.Companion.bluetoothPermissions
import uk.gov.onelogin.sharing.core.R as coreR
import uk.gov.onelogin.sharing.core.presentation.bluetooth.BluetoothSessionError
import uk.gov.onelogin.sharing.verifier.R

@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun ConnectWithHolderDeviceScreen(
    modifier: Modifier = Modifier,
    viewModel: SessionEstablishmentViewModel = metroViewModel(),
    multiplePermissionsState: MultiplePermissionsState = rememberMultiplePermissionsState(
        permissions = bluetoothPermissions()

    ) {
        viewModel.receive(ConnectWithHolderDeviceEvent.RequestedPermission(true))
    },
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
            multiplePermissionsState = multiplePermissionsState,
            modifier = Modifier,
            onSendEvent = viewModel::receive
        )

        if (contentState.isLoading) {
            CircularProgressIndicator()
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ConnectWithHolderDeviceScreenContent(
    contentState: ConnectWithHolderDeviceState,
    multiplePermissionsState: MultiplePermissionsState,
    modifier: Modifier = Modifier,
    onSendEvent: (ConnectWithHolderDeviceEvent) -> Unit = {}
) {
    val latestOnSendEvent by rememberUpdatedState(onSendEvent)

    val permissionsGranted = multiplePermissionsState.allPermissionsGranted
    val permissionsStatus = multiplePermissionsState.permissions.map {
        it.status
    }
    LaunchedEffect(permissionsStatus) {
        latestOnSendEvent(ConnectWithHolderDeviceEvent.UpdatePermission(multiplePermissionsState))
    }

    LaunchedEffect(Unit) {
        if (!permissionsGranted) {
            multiplePermissionsState.launchMultiplePermissionRequest()
        }
    }

    if (permissionsGranted && !contentState.isBluetoothEnabled) {
        EnableBluetoothPrompt()
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(spacingDouble)
    ) {
        showBluetoothDeviceState { contentState.isBluetoothEnabled }
        showBluetoothPermissionState(permissionsGranted)
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

private fun LazyListScope.showBluetoothPermissionState(permissionState: Boolean) {
    item {
        val permissionStateText = when {
            permissionState ->
                coreR.string.granted

            else -> coreR.string.denied
        }.let { stringResource(it) }

        Text(
            stringResource(
                R.string.connect_with_holder_permission_state,
                permissionStateText
            )
        )
    }
}
