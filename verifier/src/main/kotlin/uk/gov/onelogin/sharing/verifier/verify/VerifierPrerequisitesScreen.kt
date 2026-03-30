package uk.gov.onelogin.sharing.verifier.verify

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.coroutines.launch
import uk.gov.android.ui.theme.util.UnstableDesignSystemAPI

@OptIn(ExperimentalPermissionsApi::class, UnstableDesignSystemAPI::class)
@Suppress("ComposableLambdaParameterNaming")
@Composable
internal fun VerifierPrerequisitesScreen(
    modifier: Modifier = Modifier,
    viewModel: VerifierPrerequisitesViewModel = metroViewModel(),
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

    CircularProgressIndicator(modifier = modifier)
}
