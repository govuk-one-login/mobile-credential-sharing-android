@file:OptIn(ExperimentalPermissionsApi::class)

package uk.gov.onelogin.sharing.holder.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import uk.gov.onelogin.sharing.bluetooth.api.permissions.BluetoothPermissions.getBluetoothPermissions
import uk.gov.onelogin.sharing.core.presentation.bluetooth.BluetoothSessionError
import uk.gov.onelogin.sharing.core.presentation.permissions.PermissionPrompt
import uk.gov.onelogin.sharing.core.presentation.permissions.PermissionPromptText
import uk.gov.onelogin.sharing.holder.QrCodeImage
import uk.gov.onelogin.sharing.holder.R
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

private const val QR_SIZE = 800

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HolderWelcomeScreen(
    viewModel: HolderWelcomeViewModel = assistedMetroViewModel(),
    onAwaitingUserConsent: () -> Unit = {},
    onConnectionError: (BluetoothSessionError) -> Unit = {},
    onGenericError: () -> Unit = {}
) {
    val contentState by viewModel.uiState.collectAsStateWithLifecycle()
    val sessionState by viewModel.holderSessionState.collectAsStateWithLifecycle()
    val currentOnAwaitingUserConsent by rememberUpdatedState(onAwaitingUserConsent)
    var hasPreviouslyRequestedPermission by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val multiplePermissionsState = rememberMultiplePermissionsState(
        permissions = getBluetoothPermissions()
    ) {
        hasPreviouslyRequestedPermission = true
    }
    val latestOnConnectionError by rememberUpdatedState(onConnectionError)
    val latestOnGenericError by rememberUpdatedState(onGenericError)

    LaunchedEffect(Unit) {
        viewModel.navEvents.collect {
            when (it) {
                is HolderScreenEvents.NavigateToBluetoothError ->
                    latestOnConnectionError(it.error)

                is HolderScreenEvents.NavigateToGenericError ->
                    latestOnGenericError()
            }
        }
    }

    LaunchedEffect(sessionState) {
        if (sessionState is HolderSessionState.AwaitingUserConsent) {
            currentOnAwaitingUserConsent()
        }
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
}

@Composable
fun HolderScreenContent(
    contentState: HolderWelcomeUiState,
    multiplePermissionsState: MultiplePermissionsState,
    hasPreviouslyRequestedPermission: Boolean,
    grantedAllPerms: () -> Unit
) {
    when {
        contentState.hasBluetoothPermissions == true -> {
            QrContent(contentState)
        }

        else -> {
            PermissionPrompt(
                multiplePermissionsState = multiplePermissionsState,
                hasPreviouslyRequestedPermission = hasPreviouslyRequestedPermission,
                text = PermissionPromptText(
                    permanentlyDeniedText = stringResource(
                        R.string.bluetooth_permission_permanently_denied
                    ),
                    enablePermissionText = stringResource(R.string.enable_bluetooth_permission),
                    openSettingsText = stringResource(R.string.open_app_permissions),
                    deniedText = stringResource(R.string.bluetooth_permission_denied)
                ),
                onGrantedPermissions = {
                    grantedAllPerms()
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
        qrData = "QR Data"
    )

    QrContent(contentState, Modifier)
}
