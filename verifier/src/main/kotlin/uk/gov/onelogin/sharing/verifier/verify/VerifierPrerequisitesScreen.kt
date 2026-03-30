package uk.gov.onelogin.sharing.verifier.verify

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.coroutines.launch
import uk.gov.android.ui.theme.util.UnstableDesignSystemAPI
import uk.gov.onelogin.sharing.bluetooth.EnableBluetoothPrompt
import uk.gov.onelogin.sharing.bluetooth.permissions.BluetoothPermissionPrompt
import uk.gov.onelogin.sharing.orchestration.Orchestrator.Verifier.Companion.requiredPermissions

@OptIn(ExperimentalPermissionsApi::class, UnstableDesignSystemAPI::class)
@Suppress("ComposableLambdaParameterNaming")
@Composable
fun VerifierPrerequisitesScreen(
    viewModel: VerifierPrerequisitesViewModel = metroViewModel(),
    multiplePermissionsState: MultiplePermissionsState = rememberMultiplePermissionsState(
        permissions = requiredPermissions
    ) {
        viewModel.onPermissionRequestLaunched()
    },
    onNavigateToPreflight: () -> Unit = {},
    onNavigateToScanner: () -> Unit = {}
) {
    val latestOnNavigateToPreflight by rememberUpdatedState(onNavigateToPreflight)
    val latestOnNavigateToScanner by rememberUpdatedState(onNavigateToScanner)
    val coroutineScope = rememberCoroutineScope()

    val navigationEvent by viewModel.events.collectAsStateWithLifecycle()

    LaunchedEffect(navigationEvent) {
        coroutineScope.launch {
            when (navigationEvent) {
                VerifyCredentialEvents.NavigateToScanner -> {
                    latestOnNavigateToScanner()
                }

                VerifyCredentialEvents.NavigateToPreflight -> {
                    latestOnNavigateToPreflight()
                }

                else -> {
                    // do nothing with null events
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
