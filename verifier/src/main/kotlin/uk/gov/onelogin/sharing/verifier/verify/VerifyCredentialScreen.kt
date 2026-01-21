package uk.gov.onelogin.sharing.verifier.verify

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dev.zacsweers.metrox.viewmodel.metroViewModel
import uk.gov.android.ui.theme.util.UnstableDesignSystemAPI
import uk.gov.onelogin.sharing.bluetooth.EnableBluetoothPrompt
import uk.gov.onelogin.sharing.bluetooth.api.permissions.PermissionChecker.Companion.centralPermissions
import uk.gov.onelogin.sharing.bluetooth.permissions.BluetoothPermissionPrompt

@OptIn(ExperimentalPermissionsApi::class, UnstableDesignSystemAPI::class)
@Suppress("ComposableLambdaParameterNaming")
@Composable
fun VerifyCredentialScreen(
    viewModel: VerifyCredentialViewModel = metroViewModel(),
    multiplePermissionsState: MultiplePermissionsState = rememberMultiplePermissionsState(
        permissions = centralPermissions()

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

        VerifyCredentialPreconditionsState.BluetoothAccessDenied -> {
            BluetoothPermissionPrompt(
                multiplePermissionsState,
                hasPreviouslyRequestedPermission = uiState.hasPreviouslyRequestedPermission
            ) {}
        }

        VerifyCredentialPreconditionsState.BluetoothDisabled -> {
            EnableBluetoothPrompt()
        }

        VerifyCredentialPreconditionsState.Met -> Unit
    }
}
