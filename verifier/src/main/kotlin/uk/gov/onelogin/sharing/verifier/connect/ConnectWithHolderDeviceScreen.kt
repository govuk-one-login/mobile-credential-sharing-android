package uk.gov.onelogin.sharing.verifier.connect

import android.Manifest
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import dev.zacsweers.metrox.viewmodel.metroViewModel
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.android.ui.theme.spacingDouble
import uk.gov.android.ui.theme.spacingSingle
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.permissions.BluetoothPermissionPrompt
import uk.gov.onelogin.sharing.security.cbor.decodeDeviceEngagement
import uk.gov.onelogin.sharing.security.cbor.dto.DeviceEngagementDto
import uk.gov.onelogin.sharing.security.cbor.dto.DeviceRetrievalMethodDto
import uk.gov.onelogin.sharing.verifier.R
import uk.gov.onelogin.sharing.core.R as coreR

@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun ConnectWithHolderDeviceScreen(
    viewModel: SessionEstablishmentViewModel = metroViewModel(),
    base64EncodedEngagement: String,
    modifier: Modifier = Modifier,
    permissionState: PermissionState = rememberPermissionState(
        permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Manifest.permission.BLUETOOTH_SCAN
        } else {
            Manifest.permission.BLUETOOTH
        }
    ),
) {
    val contentState by viewModel.uiState.collectAsStateWithLifecycle()
    var hasPreviouslyRequestedPermission by remember { mutableStateOf(false) }

    val multiplePermissionsState = rememberMultiplePermissionsState(
        permissions = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_CONNECT)
                add(Manifest.permission.BLUETOOTH_ADVERTISE)
                add(Manifest.permission.ACCESS_FINE_LOCATION)
            } else {
                add(Manifest.permission.BLUETOOTH)
            }
        }
    ) {
        hasPreviouslyRequestedPermission = true
    }

    val engagementData = remember {
        decodeDeviceEngagement(
            base64EncodedEngagement,
            logger = SystemLogger()
        )
    }

    LaunchedEffect(Unit) {
        if (!multiplePermissionsState.allPermissionsGranted) {
            multiplePermissionsState.launchMultiplePermissionRequest()
        }
    }

    DisposableEffect(engagementData, multiplePermissionsState.allPermissionsGranted) {
        val uuidToScan = engagementData?.deviceRetrievalMethods
            ?.firstNotNullOfOrNull { it.getPeripheralServerModeUuid() }

        if (multiplePermissionsState.allPermissionsGranted &&
            contentState.isBluetoothEnabled &&
            uuidToScan != null
        ) {
            viewModel.scanForDevice(uuidToScan)
        }
        onDispose {
            viewModel.stopScanning()
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
        showBluetoothPermissionState(multiplePermissionsState, hasPreviouslyRequestedPermission)
        showBluetoothDeviceState { contentState.isBluetoothEnabled }

        if (permissionState.status.isGranted && contentState.isBluetoothEnabled) {
            showUuidsToScan(
                engagementData?.deviceRetrievalMethods
            )
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

@OptIn(ExperimentalPermissionsApi::class)
private fun LazyListScope.showBluetoothPermissionState(
    multiplePermissionsState: MultiplePermissionsState,
    hasPreviouslyRequestedPermission: Boolean
) {
    item {
        BluetoothPermissionPrompt(
            multiplePermissionsState = multiplePermissionsState,
            hasPreviouslyRequestedPermission = hasPreviouslyRequestedPermission
        ) {

        }
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
                deviceRetrievalMethodDto.getPeripheralServerModeUuidString()?.let {
                    Text("UUID: $it")
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalPermissionsApi::class)
@Preview
internal fun ConnectWithHolderDevicePreview(
    @PreviewParameter(ConnectWithHolderDevicePreviewParameters::class)
    state: ConnectWithHolderDeviceState,
    modifier: Modifier = Modifier
) {
    GdsTheme {
        ConnectWithHolderDeviceScreen(
            base64EncodedEngagement = state.base64EncodedEngagement!!,
            modifier = modifier.background(Color.White),
            permissionState = state.permissionState!!
        )
    }
}
