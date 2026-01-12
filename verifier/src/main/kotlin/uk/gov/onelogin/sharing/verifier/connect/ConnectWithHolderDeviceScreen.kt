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
import androidx.compose.ui.Alignment
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
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.EnableBluetoothPrompt
import uk.gov.onelogin.sharing.bluetooth.api.permissions.PermissionChecker
import uk.gov.onelogin.sharing.bluetooth.permissions.BluetoothPermissionPrompt
import uk.gov.onelogin.sharing.core.UUIDExtensions.toUUID
import uk.gov.onelogin.sharing.security.cbor.decodeDeviceEngagement
import uk.gov.onelogin.sharing.security.cbor.dto.DeviceEngagementDto
import uk.gov.onelogin.sharing.security.cbor.dto.DeviceRetrievalMethodDto
import uk.gov.onelogin.sharing.verifier.R
import uk.gov.onelogin.sharing.core.R as coreR

@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun ConnectWithHolderDeviceScreen(
    base64EncodedEngagement: String,
    modifier: Modifier = Modifier,
    viewModel: SessionEstablishmentViewModel = metroViewModel(),
    multiplePermissionsState: MultiplePermissionsState = rememberMultiplePermissionsState(
        permissions = PermissionChecker.advertiseFineLocationPermissions()
    ) {
        viewModel.updateHasRequestPermissions(true)
    }
) {
    viewModel.update(base64EncodedEngagement)
    val contentState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize()) {
        BluetoothPermissionPrompt(
            multiplePermissionsState,
            contentState.hasRequestedPermissions
        ) {
            if (contentState.showErrorScreen == ConnectWithHolderDeviceError.GenericError) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Generic Error"
                    )
                }
            } else {
                val engagementData: DeviceEngagementDto? by viewModel
                    .engagementData.collectAsStateWithLifecycle()

                ConnectWithHolderDeviceScreenContent(
                    base64EncodedEngagement = base64EncodedEngagement,
                    contentState = contentState,
                    engagementData = engagementData,
                    multiplePermissionsState = multiplePermissionsState,
                    modifier = Modifier,
                    onUpdatePermissions = viewModel::update,
                    onScanForDevice = viewModel::scanForDevice,
                    onStopScanning = viewModel::stopScanning,
                )
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ConnectWithHolderDeviceScreenContent(
    base64EncodedEngagement: String,
    contentState: ConnectWithHolderDeviceState,
    engagementData: DeviceEngagementDto?,
    multiplePermissionsState: MultiplePermissionsState,
    modifier: Modifier = Modifier,
    onUpdatePermissions: (MultiplePermissionsState) -> Unit = {},
    onScanForDevice: (ByteArray) -> Unit = {},
    onStopScanning: () -> Unit = {},
) {
    val permissionsGranted = multiplePermissionsState.allPermissionsGranted
    val permissionsStatus = multiplePermissionsState.permissions.map {
        it.status
    }
    LaunchedEffect(permissionsStatus) {
        onUpdatePermissions(multiplePermissionsState)
    }

    LaunchedEffect(Unit) {
        if (!permissionsGranted) {
            multiplePermissionsState.launchMultiplePermissionRequest()
        }
    }

    if (permissionsGranted && !contentState.isBluetoothEnabled) {
        EnableBluetoothPrompt()
    }

    DisposableEffect(engagementData, permissionsGranted) {
        val uuidToScan = engagementData?.getFirstPeripheralServerModeUuid()

        if (permissionsGranted && contentState.isBluetoothEnabled &&
            uuidToScan != null
        ) {
            onScanForDevice(uuidToScan)
        }

        onDispose {
            onStopScanning()
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(spacingDouble)
    ) {
        item {
            Text(stringResource(R.string.connect_with_holder_heading))
        }
        item {
            Text(base64EncodedEngagement)
        }
        showBluetoothDeviceState { contentState.isBluetoothEnabled }
        showBluetoothPermissionState(permissionsGranted)
        if (permissionsGranted && contentState.isBluetoothEnabled) {
            showUuidsToScan(engagementData?.deviceRetrievalMethods)
        }
        showEngagementData(engagementData)
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
    base64EncodedEngagement: String,
) {
    val engagementData = remember {
        decodeDeviceEngagement(
            base64EncodedEngagement,
            logger = SystemLogger()
        )
    }

    GdsTheme {
        ConnectWithHolderDeviceScreenContent(
            base64EncodedEngagement = base64EncodedEngagement,
            contentState = ConnectWithHolderDeviceState(),
            engagementData = engagementData,
            multiplePermissionsState = rememberMultiplePermissionsState(
                permissions = PermissionChecker.advertiseFineLocationPermissions()
            ) {},
            modifier = Modifier.background(Color.White)
        )
    }
}
