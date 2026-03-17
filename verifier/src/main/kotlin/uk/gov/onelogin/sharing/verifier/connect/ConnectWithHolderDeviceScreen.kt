package uk.gov.onelogin.sharing.verifier.connect

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dev.zacsweers.metrox.viewmodel.metroViewModel
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.android.ui.theme.spacingDouble
import uk.gov.android.ui.theme.spacingSingle
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.EnableBluetoothPrompt
import uk.gov.onelogin.sharing.bluetooth.api.permissions.bluetooth.BluetoothPermissionChecker.Companion.bluetoothPermissions
import uk.gov.onelogin.sharing.core.R as coreR
import uk.gov.onelogin.sharing.core.UUIDExtensions.toUUID
import uk.gov.onelogin.sharing.security.cbor.decodeDeviceEngagement
import uk.gov.onelogin.sharing.security.cbor.dto.DeviceEngagementDto
import uk.gov.onelogin.sharing.security.cbor.dto.DeviceRetrievalMethodDto
import uk.gov.onelogin.sharing.verifier.R

@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun ConnectWithHolderDeviceScreen(
    base64EncodedEngagement: String,
    modifier: Modifier = Modifier,
    viewModel: SessionEstablishmentViewModel = metroViewModel(),
    multiplePermissionsState: MultiplePermissionsState = rememberMultiplePermissionsState(
        permissions = bluetoothPermissions()

    ) {
        viewModel.receive(ConnectWithHolderDeviceEvent.RequestedPermission(true))
    },
    onConnectionError: (ConnectWithHolderDeviceError) -> Unit = {}
) {
    val latestOnConnectionError by rememberUpdatedState(onConnectionError)

    val contentState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(base64EncodedEngagement) {
        viewModel.receive(
            ConnectWithHolderDeviceEvent.UpdateEngagementData(base64EncodedEngagement)
        )
    }

    LaunchedEffect(Unit) {
        viewModel.navEvents.collect {
            when (it) {
                is ConnectWithHolderDeviceNavEvent.NavigateToError ->
                    latestOnConnectionError(it.error)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        ConnectWithHolderDeviceScreenContent(
            contentState = contentState,
            multiplePermissionsState = multiplePermissionsState,
            modifier = Modifier,
            onSendEvent = viewModel::receive
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetOrchestrator()
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

    DisposableEffect(contentState.engagementData, permissionsGranted) {
        val uuidToScan = contentState.engagementData?.getFirstPeripheralServerModeUuid()

        if (permissionsGranted && contentState.isBluetoothEnabled &&
            uuidToScan != null
        ) {
            latestOnSendEvent(ConnectWithHolderDeviceEvent.StartScanning(uuidToScan))
        }

        onDispose {
            latestOnSendEvent(ConnectWithHolderDeviceEvent.StopScanning)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(spacingDouble)
    ) {
        item {
            Text(stringResource(R.string.connect_with_holder_heading))
        }

        if (contentState.base64EncodedEngagement != null) {
            item {
                Text(contentState.base64EncodedEngagement)
            }
        }

        showBluetoothDeviceState { contentState.isBluetoothEnabled }
        showBluetoothPermissionState(permissionsGranted)
        if (permissionsGranted && contentState.isBluetoothEnabled) {
            showUuidsToScan(contentState.engagementData?.deviceRetrievalMethods)
        }
        showEngagementData(contentState.engagementData)
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

private fun LazyListScope.showEngagementData(engagementData: DeviceEngagementDto?) {
    if (engagementData == null) {
        item {
            Text(stringResource(R.string.connect_with_holder_error_decode))
        }
    } else {
        item {
            Text(stringResource(R.string.connect_with_holder_decoded_data))
        }
        item {
            Text(engagementData.toString())
        }
    }
}

private fun LazyListScope.showUuidsToScan(deviceRetrievalMethods: List<DeviceRetrievalMethodDto>?) {
    item {
        Column(
            verticalArrangement = Arrangement.spacedBy(spacingSingle)
        ) {
            Text(
                stringResource(R.string.connect_with_holder_searching_for_uuids)
            )

            deviceRetrievalMethods?.forEach { deviceRetrievalMethodDto ->
                val uuid = deviceRetrievalMethodDto.getPeripheralServerModeUuid()?.toUUID()
                Text("UUID: $uuid")
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
@Preview
internal fun ConnectWithHolderDevicePreview(
    @PreviewParameter(ConnectWithHolderDevicePreviewParameters::class)
    base64EncodedEngagement: String
) {
    val engagementData = remember {
        decodeDeviceEngagement(
            base64EncodedEngagement,
            logger = SystemLogger()
        )
    }

    GdsTheme {
        ConnectWithHolderDeviceScreenContent(
            contentState = ConnectWithHolderDeviceState(
                base64EncodedEngagement = base64EncodedEngagement,
                engagementData = engagementData
            ),
            multiplePermissionsState = rememberMultiplePermissionsState(
                permissions = bluetoothPermissions()
            ) {},
            modifier = Modifier.background(Color.White)
        )
    }
}
