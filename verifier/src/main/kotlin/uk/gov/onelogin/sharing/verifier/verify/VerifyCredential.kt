package uk.gov.onelogin.sharing.verifier.verify

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import uk.gov.onelogin.sharing.bluetooth.EnableBluetoothPrompt
import uk.gov.onelogin.sharing.bluetooth.permissions.BluetoothPermissionPrompt
import uk.gov.onelogin.sharing.verifier.scan.VerifierScanner

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VerifyCredential(
    modifier: Modifier = Modifier,
    viewModel: VerifyCredentialViewModel = viewModel<VerifyCredentialViewModel>()
) {
    when (viewModel.uiState.collectAsStateWithLifecycle().value.preconditionsState) {
        is VerifyCredentialPreconditionsState.BluetoothAccessDenied -> {
            // DCMAW-17594: Bluetooth enabled check occurs too late in the flow
            //
            // BluetoothPermissionPrompt to be used here
            // See `HolderWelcomeScreen.kt`
        }

        is VerifyCredentialPreconditionsState.BluetoothDisabled -> {
            EnableBluetoothPrompt()
        }

        is VerifyCredentialPreconditionsState.Met -> {
            // decide which attributes will be requested for verification
            // TODO: find ticket number for this work- DCMAW-XXXXX |

            // then display scan screen
            VerifierScanner(
                modifier = modifier
                // why do we have different routes for the error pages?
            )
        }
    }
}
