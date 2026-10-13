package uk.gov.onelogin.sharing.holder.getreadytoshare

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.android.ui.theme.spacingSingle
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.putScreenState
import uk.gov.onelogin.sharing.prerequisites.api.Prerequisite
import uk.gov.onelogin.sharing.prerequisites.api.PrerequisiteAction
import uk.gov.onelogin.sharing.prerequisites.api.contracts.PrerequisiteActionContract
import uk.gov.onelogin.sharing.prerequisites.api.ui.RetryPrerequisitesContent

@Composable
internal fun GetReadyToShareScreen(
    modifier: Modifier = Modifier,
    viewModel: GetReadyToShareViewModel = metroViewModel(),
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

    val metrics = JankStatsHelper.rememberMetricsStateHolder()
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

    GetReadyToShareContent(
        modifier = modifier,
        missingPrerequisites = missingPrerequisites,
        onButtonClick = { viewModel.resolve(launcher) }
    )
}

@Composable
internal fun GetReadyToShareContent(
    missingPrerequisites: List<Prerequisite>?,
    modifier: Modifier = Modifier,
    onButtonClick: () -> Unit = {},
) {
    RetryPrerequisitesContent(
        missingPrerequisites = missingPrerequisites,
        modifier = modifier,
        onButtonClick = onButtonClick
    )
}

@Preview(showBackground = true)
@Composable
fun GetReadyToSharePreview() {
    GdsTheme {
        GetReadyToShareContent(
            missingPrerequisites = listOf(Prerequisite.BLUETOOTH),
            modifier = Modifier.padding(spacingSingle)
        )
    }
}