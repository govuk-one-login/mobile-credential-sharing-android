package uk.gov.onelogin.sharing.verifier.verify

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dev.zacsweers.metrox.viewmodel.metroViewModel
import uk.gov.android.ui.theme.util.UnstableDesignSystemAPI
import uk.gov.onelogin.sharing.bluetooth.EnableBluetoothPrompt
import uk.gov.onelogin.sharing.core.presentation.permissions.PermissionPrompt
import uk.gov.onelogin.sharing.core.presentation.permissions.PermissionPromptText
import uk.gov.onelogin.sharing.orchestration.Orchestrator.Verifier.Companion.requiredPermissions
import uk.gov.onelogin.sharing.verifier.R

@OptIn(ExperimentalPermissionsApi::class, UnstableDesignSystemAPI::class)
@Suppress("ComposableLambdaParameterNaming")
@Composable
fun VerifyCredentialScreen(
    viewModel: VerifyCredentialViewModel = metroViewModel(),
    multiplePermissionsState: MultiplePermissionsState = rememberMultiplePermissionsState(
        permissions = requiredPermissions
    ) {
        viewModel.onPermissionRequestLaunched()
    },
    navigateToScanner: () -> Unit = {}
) {
    val latestOnNavigateToScanner by rememberUpdatedState(navigateToScanner)
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                VerifyCredentialEvents.NavigateToScanner -> {
                    latestOnNavigateToScanner()
                }
            }
        }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val permissionsStatus = multiplePermissionsState.permissions.map {
        it.status
    }
    LaunchedEffect(permissionsStatus) {
        viewModel.onPermissionsChanged(multiplePermissionsState)
    }

    LaunchedEffect(Unit) {
        if (!multiplePermissionsState.allPermissionsGranted) {
            multiplePermissionsState.launchMultiplePermissionRequest()
        }
    }

    when (uiState.preconditionsState) {
        VerifyCredentialPreconditionsState.Idle -> Unit

        VerifyCredentialPreconditionsState.CameraAccessDenied -> {
            PermissionPrompt(
                multiplePermissionsState = multiplePermissionsState,
                hasPreviouslyRequestedPermission = uiState.hasPreviouslyRequestedPermission,
                text = PermissionPromptText(
                    permanentlyDeniedText = stringResource(
                        R.string.camera_permission_is_permanently_denied
                    ),
                    enablePermissionText = stringResource(
                        R.string.enable_camera_permission_to_continue
                    ),
                    openSettingsText = stringResource(R.string.open_app_permissions),
                    deniedText = stringResource(R.string.camera_permission_denied)
                )
            ) {}
        }

        VerifyCredentialPreconditionsState.BluetoothAccessDenied -> {
            PermissionPrompt(
                multiplePermissionsState = multiplePermissionsState,
                hasPreviouslyRequestedPermission = uiState.hasPreviouslyRequestedPermission,
                text = PermissionPromptText(
                    permanentlyDeniedText = stringResource(
                        R.string.bluetooth_permission_permanently_denied
                    ),
                    enablePermissionText = stringResource(R.string.enable_bluetooth_permission),
                    openSettingsText = stringResource(R.string.open_app_permissions),
                    deniedText = stringResource(R.string.bluetooth_permission_denied)
                )
            ) {}
        }

        VerifyCredentialPreconditionsState.BluetoothDisabled -> {
            EnableBluetoothPrompt()
        }

        VerifyCredentialPreconditionsState.Met -> Unit
    }
}
