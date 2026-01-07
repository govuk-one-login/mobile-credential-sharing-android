package uk.gov.onelogin.sharing.verifier.verify

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import dev.zacsweers.metrox.viewmodel.metroViewModel
import uk.gov.android.ui.theme.util.UnstableDesignSystemAPI
import uk.gov.onelogin.sharing.bluetooth.EnableBluetoothPrompt
import uk.gov.onelogin.sharing.verifier.scan.VerifierScanner

@OptIn(ExperimentalPermissionsApi::class, UnstableDesignSystemAPI::class)
@Suppress("ComposableLambdaParameterNaming")
@Composable
fun VerifyCredential(
    modifier: Modifier = Modifier,
    viewModel: VerifyCredentialViewModel = metroViewModel(),
    scannerContent: @Composable () -> Unit = { VerifierScanner(modifier = modifier) }
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
            scannerContent()
        }
    }
}
