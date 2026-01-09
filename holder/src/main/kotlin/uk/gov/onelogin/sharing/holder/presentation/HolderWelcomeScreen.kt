@file:OptIn(ExperimentalPermissionsApi::class)

package uk.gov.onelogin.sharing.holder.presentation

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import java.util.UUID
import uk.gov.onelogin.sharing.bluetooth.BluetoothUiErrorTypes.BLUETOOTH_DISCONNECTED
import uk.gov.onelogin.sharing.bluetooth.BluetoothUiErrorTypes.BLUETOOTH_TURNED_OFF
import uk.gov.onelogin.sharing.bluetooth.BluetoothUiErrorTypes.PERMISSIONS_MISSING
import uk.gov.onelogin.sharing.bluetooth.EnableBluetoothPrompt
import uk.gov.onelogin.sharing.core.presentation.ErrorScreen
import uk.gov.onelogin.sharing.core.presentation.buttons.PermanentPermissionDenialButton
import uk.gov.onelogin.sharing.core.presentation.buttons.PermissionRationaleButton
import uk.gov.onelogin.sharing.core.presentation.buttons.RequirePermissionButton
import uk.gov.onelogin.sharing.holder.QrCodeImage
import uk.gov.onelogin.sharing.holder.R
import uk.gov.onelogin.sharing.holder.R.string.bluetooth_disconnected_unexpectedly
import uk.gov.onelogin.sharing.holder.R.string.bluetooth_permissions_revoked
import uk.gov.onelogin.sharing.holder.R.string.bluetooth_turned_off
import uk.gov.onelogin.sharing.holder.mdoc.MdocSessionState

private const val QR_SIZE = 800

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HolderWelcomeScreen(viewModel: HolderWelcomeViewModel = assistedMetroViewModel()) {
    val contentState by viewModel.uiState.collectAsStateWithLifecycle()
    var hasPreviouslyRequestedPermission by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current
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

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val granted = multiplePermissionsState.allPermissionsGranted

                viewModel.updateBluetoothPermissions(granted)
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    HolderScreenContent(
        contentState,
        multiplePermissionsState,
        hasPreviouslyRequestedPermission
    ) { viewModel.updateBluetoothPermissions(true) }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.onScreenDisposed()
        }
    }
}

@Composable
fun HolderScreenContent(
    contentState: HolderWelcomeUiState,
    multiplePermissionsState: MultiplePermissionsState,
    hasPreviouslyRequestedPermission: Boolean,
    grantedAllPerms: () -> Unit
) {
    when {
        contentState.showErrorScreen -> {
            val errorText = when (contentState.bluetoothErrorType) {
                BLUETOOTH_DISCONNECTED -> bluetooth_disconnected_unexpectedly
                PERMISSIONS_MISSING -> bluetooth_permissions_revoked
                BLUETOOTH_TURNED_OFF -> bluetooth_turned_off
            }

            ErrorScreen(errorText = stringResource(errorText))
        }

        contentState.showEnableBluetoothPrompt &&
            contentState.hasBluetoothPermissions == true -> {
            EnableBluetoothPrompt(denialText = bluetooth_turned_off)
        }

        contentState.hasBluetoothPermissions == true -> {
            QrContent(contentState)
        }

        else -> {
            BluetoothPermissionPrompt(
                multiplePermissionsState = multiplePermissionsState,
                hasPreviouslyRequestedPermission = hasPreviouslyRequestedPermission,
                onGrantedPermissions = {
                    grantedAllPerms()
                }
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
fun MultiplePermissionsState.isPermanentlyDenied(): Boolean = permissions.any { perm ->
    !perm.status.isGranted &&
        !perm.status.shouldShowRationale
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
        sessionState = MdocSessionState.AdvertisingStarted,
        uuid = UUID.randomUUID(),
        qrData = "QR Data"
    )

    QrContent(contentState, Modifier)
}
