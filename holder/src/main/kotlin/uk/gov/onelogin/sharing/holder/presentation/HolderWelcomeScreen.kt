@file:OptIn(ExperimentalPermissionsApi::class)

package uk.gov.onelogin.sharing.holder.presentation

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale
import java.util.UUID
import uk.gov.onelogin.sharing.bluetooth.BluetoothStateManagerPrompt
import uk.gov.onelogin.sharing.bluetooth.BluetoothStatus
import uk.gov.onelogin.sharing.bluetooth.api.MdocSessionState
import uk.gov.onelogin.sharing.core.presentation.buttons.PermanentPermissionDenialButton
import uk.gov.onelogin.sharing.core.presentation.buttons.PermissionRationaleButton
import uk.gov.onelogin.sharing.core.presentation.buttons.RequirePermissionButton
import uk.gov.onelogin.sharing.holder.QrCodeImage
import uk.gov.onelogin.sharing.holder.R

private const val QR_SIZE = 800

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HolderWelcomeScreen(
    viewModel: HolderWelcomeViewModel = HolderWelcomeViewModel.holderWelcomeViewModel()
) {
    val contentState by viewModel.uiState.collectAsStateWithLifecycle()
    var hasPreviouslyRequestedPermission by remember { mutableStateOf(false) }

    val multiplePermissionsState = rememberMultiplePermissionsState(
        permissions = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_CONNECT)
                add(Manifest.permission.BLUETOOTH_ADVERTISE)
            } else {
                add(Manifest.permission.BLUETOOTH)
            }
        }
    ) {
        hasPreviouslyRequestedPermission = true
    }

    BluetoothPermissionPrompt(
        multiplePermissionsState = multiplePermissionsState,
        hasPreviouslyRequestedPermission = hasPreviouslyRequestedPermission,
        onGrantedPermissions = {
            viewModel.updateBluetoothPermissions(true)
        }
    )

    if (contentState.hasBluetoothPermissions == true) {
        BluetoothStateManagerPrompt {
            when (it) {
                BluetoothStatus.BLUETOOTH_ON -> viewModel.updateBluetoothState(
                    BluetoothState.Enabled
                )

                BluetoothStatus.BLUETOOTH_OFF -> viewModel.updateBluetoothState(
                    BluetoothState.Disabled
                )

                BluetoothStatus.INITIALIZING -> viewModel.updateBluetoothState(
                    BluetoothState.Initializing
                )
            }
        }

        HolderScreenContent(contentState)
    }
}

@Composable
fun HolderScreenContent(contentState: HolderWelcomeUiState) {
    when (contentState.bluetoothStatus) {
        BluetoothState.Disabled -> {
            println("User denied turning on bluetooth via prompt")
            BluetoothDisabledScreen()
        }

        BluetoothState.Enabled -> {
            println("User turned bluetooth on via prompt")
            QrContent(contentState, Modifier)
        }

        BluetoothState.Initializing -> Unit
    }
}

@OptIn(ExperimentalPermissionsApi::class)
fun MultiplePermissionsState.isPermanentlyDenied(): Boolean = permissions.any { perm ->
    !perm.status.isGranted &&
        !perm.status.shouldShowRationale
}

@Composable
fun BluetoothDisabledScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(R.string.bluetooth_disabled_error_text))
    }
}

@Suppress("LongMethod", "ComposableLambdaParameterNaming")
@Composable
fun BluetoothPermissionPrompt(
    multiplePermissionsState: MultiplePermissionsState,
    hasPreviouslyRequestedPermission: Boolean,
    modifier: Modifier = Modifier,
    onGrantedPermissions: @Composable () -> Unit
) {
    when {
        multiplePermissionsState.allPermissionsGranted -> {
            onGrantedPermissions()
        }

        multiplePermissionsState.shouldShowRationale -> {
            PermissionRationaleButton(
                text = stringResource(R.string.enable_bluetooth_permission),
                launchPermission = {
                    multiplePermissionsState.launchMultiplePermissionRequest()
                }
            )
        }

        hasPreviouslyRequestedPermission && multiplePermissionsState.isPermanentlyDenied() -> {
            PermanentPermissionDenialButton(
                context = LocalContext.current,
                modifier = modifier,
                titleText = stringResource(R.string.bluetooth_permission_permanently_denied),
                buttonText = stringResource(R.string.open_app_permissions)
            )
        }

        else -> {
            RequirePermissionButton(
                text = stringResource(R.string.enable_bluetooth_permission),
                launchPermission = {
                    multiplePermissionsState.launchMultiplePermissionRequest()
                }
            )
        }
    }
}

@Composable
fun QrContent(contentState: HolderWelcomeUiState, modifier: Modifier = Modifier) {
    HolderWelcomeText()
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

@Composable
@Preview
internal fun HolderWelcomeScreenPreview() {
    val contentState = HolderWelcomeUiState(
        lastErrorMessage = null,
        sessionState = MdocSessionState.Started,
        uuid = UUID.randomUUID(),
        qrData = "QR Data"
    )

    QrContent(contentState, Modifier)
}
