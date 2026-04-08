package uk.gov.onelogin.sharing.verifier.verify

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import dev.zacsweers.metrox.viewmodel.metroViewModel
import uk.gov.android.ui.theme.util.UnstableDesignSystemAPI

@OptIn(ExperimentalPermissionsApi::class, UnstableDesignSystemAPI::class)
@Suppress("ComposableLambdaParameterNaming")
@Composable
internal fun VerifierPrerequisitesScreen(
    modifier: Modifier = Modifier,
    viewModel: VerifierPrerequisitesViewModel = metroViewModel(),
    onNavigateToPreflight: () -> Unit = {},
    onNavigateToScanner: () -> Unit = {},
    onUnrecoverableError: () -> Unit = {}
) {
    val latestOnNavigateToPreflight by rememberUpdatedState(onNavigateToPreflight)
    val latestOnNavigateToScanner by rememberUpdatedState(onNavigateToScanner)
    val latestOnUnrecoverableError by rememberUpdatedState(onUnrecoverableError)

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                VerifyCredentialEvents.NavigateToScanner -> latestOnNavigateToScanner()

                VerifyCredentialEvents.NavigateToPreflight -> latestOnNavigateToPreflight()

                VerifyCredentialEvents.NavigateToUnrecoverableError -> latestOnUnrecoverableError()

                else -> {
                    // do nothing with null events
                }
            }
        }
    }

    CircularProgressIndicator(modifier = modifier)
}
