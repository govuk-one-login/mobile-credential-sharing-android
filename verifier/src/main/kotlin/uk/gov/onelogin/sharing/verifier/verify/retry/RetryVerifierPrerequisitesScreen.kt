package uk.gov.onelogin.sharing.verifier.verify.retry

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteAction
import uk.gov.onelogin.sharing.orchestration.prerequisites.contracts.PrerequisiteActionContract
import uk.gov.onelogin.sharing.orchestration.prerequisites.ui.RetryPrerequisitesContent
import uk.gov.onelogin.sharing.orchestration.prerequisites.usecases.RetryPrerequisitesNavigator.NavigationEvent

@Composable
internal fun RetryVerifierPrerequisitesScreen(
    modifier: Modifier = Modifier,
    viewModel: RetryVerifierPrerequisitesViewModel = metroViewModel(),
    launcher: ActivityResultLauncher<PrerequisiteAction> = rememberLauncherForActivityResult(
        PrerequisiteActionContract
    ) {
        viewModel.recheckPrerequisites()
    },
    onPassPrerequisites: () -> Unit = {},
    onUnrecoverableError: () -> Unit = {}
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnPassPrerequisites by rememberUpdatedState(onPassPrerequisites)
    val currentOnUnrecoverableError by rememberUpdatedState(onUnrecoverableError)
    val missingPrerequisites: List<Prerequisite>? by viewModel
        .prerequisites
        .collectAsStateWithLifecycle()
    val hasPreviouslyRecheckedPrerequisites: Boolean by viewModel
        .hasRecheckedPrerequisites
        .collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is NavigationEvent.PassedPrerequisites ->
                    currentOnPassPrerequisites()

                is NavigationEvent.UnrecoverableError ->
                    currentOnUnrecoverableError()

                else -> {
                    // do nothing with null events
                }
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && hasPreviouslyRecheckedPrerequisites) {
                viewModel.recheckPrerequisites()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    RetryPrerequisitesContent(
        modifier = modifier,
        missingPrerequisites = missingPrerequisites,
        onButtonClick = { viewModel.resolve(launcher) }
    )
}
