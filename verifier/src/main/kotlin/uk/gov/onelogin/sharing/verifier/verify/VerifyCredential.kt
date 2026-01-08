package uk.gov.onelogin.sharing.verifier.verify

import android.Manifest
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dev.zacsweers.metrox.viewmodel.metroViewModel
import uk.gov.android.ui.theme.util.UnstableDesignSystemAPI
import uk.gov.onelogin.sharing.bluetooth.EnableBluetoothPrompt
import uk.gov.onelogin.sharing.bluetooth.permissions.BluetoothPermissionPrompt
import uk.gov.onelogin.sharing.verifier.scan.VerifierScanner

@OptIn(ExperimentalPermissionsApi::class, UnstableDesignSystemAPI::class)
@Suppress("ComposableLambdaParameterNaming")
@Composable
fun VerifyCredential(
    modifier: Modifier = Modifier,
    viewModel: VerifyCredentialViewModel = metroViewModel(),
    scannerContent: @Composable () -> Unit = { VerifierScanner(modifier = modifier) },
    multiplePermissionsState: MultiplePermissionsState = rememberMultiplePermissionsState(
        permissions = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_SCAN)
                add(Manifest.permission.BLUETOOTH_CONNECT)
                add(Manifest.permission.ACCESS_FINE_LOCATION)
            } else {
                add(Manifest.permission.BLUETOOTH)
            }
        }
    ) {
        viewModel.onPermissionRequestLaunched()
    }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(multiplePermissionsState.allPermissionsGranted) {
        viewModel.onPermissionsChanged(multiplePermissionsState.allPermissionsGranted)
    }

    when (uiState.preconditionsState) {
        VerifyCredentialPreconditionsState.Idle -> Unit

        VerifyCredentialPreconditionsState.BluetoothAccessDenied -> {
            BluetoothPermissionPrompt(
                multiplePermissionsState,
                hasPreviouslyRequestedPermission = uiState.hasPreviouslyRequestedPermission
            ) {
                viewModel.onPermissionsChanged(true)
            }
        }

        VerifyCredentialPreconditionsState.BluetoothDisabled -> {
            EnableBluetoothPrompt()
        }

        VerifyCredentialPreconditionsState.Met -> {
            scannerContent()
        }
    }
}
