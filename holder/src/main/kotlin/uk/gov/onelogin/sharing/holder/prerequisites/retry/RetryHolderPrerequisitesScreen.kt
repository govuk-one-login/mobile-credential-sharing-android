package uk.gov.onelogin.sharing.holder.prerequisites.retry

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.putScreenState
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.rememberMetricsStateHolder
import uk.gov.onelogin.sharing.prerequisites.Prerequisite
import uk.gov.onelogin.sharing.prerequisites.PrerequisiteAction
import uk.gov.onelogin.sharing.prerequisites.contracts.PrerequisiteActionContract
import uk.gov.onelogin.sharing.prerequisites.ui.RetryPrerequisitesContent

@Composable
internal fun RetryHolderPrerequisitesScreen(
    modifier: Modifier = Modifier,
    viewModel: RetryHolderPrerequisitesViewModel = metroViewModel(),
    launcher: ActivityResultLauncher<PrerequisiteAction> = rememberLauncherForActivityResult(
        PrerequisiteActionContract
    ) {
        viewModel.recheckPrerequisites()
    }
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val missingPrerequisites: List<Prerequisite>? by viewModel
        .prerequisites
        .collectAsStateWithLifecycle()
    val hasPreviouslyRecheckedPrerequisites: Boolean by viewModel
        .hasRecheckedPrerequisites
        .collectAsStateWithLifecycle()

    val metrics = rememberMetricsStateHolder()
    LaunchedEffect(Unit) {
        metrics.putScreenState("RetryHolderPrerequisitesScreen")
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
