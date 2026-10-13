package uk.gov.onelogin.sharing.holder.getreadytoshare

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.immutableListOf
import kotlinx.collections.immutable.persistentListOf
import uk.gov.android.ui.componentsv2.heading.GdsHeading
import uk.gov.android.ui.componentsv2.heading.GdsHeadingAlignment
import uk.gov.android.ui.patterns.leftalignedscreen.LeftAlignedScreen
import uk.gov.android.ui.patterns.leftalignedscreen.LeftAlignedScreenBodyV2
import uk.gov.android.ui.patterns.leftalignedscreen.LeftAlignedScreenButton
import uk.gov.android.ui.patterns.leftalignedscreen.LeftAlignedScreenV2
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.android.ui.theme.spacingSingle
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.putScreenState
import uk.gov.onelogin.sharing.holder.R
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
    onFindOutMoreButtonClick: () -> Unit = {},
) {
    LeftAlignedScreenV2(
        modifier = modifier,
        title = stringResource(R.string.get_ready_to_share_header),
        body = persistentListOf(),
        primaryButton = LeftAlignedScreenButton(
            text = stringResource(R.string.get_ready_to_share_allow_bluetooth),
            onClick = onButtonClick
        ),
        secondaryButton = LeftAlignedScreenButton(
            text = stringResource(R.string.get_ready_to_share_find_out_more),
            onClick = onFindOutMoreButtonClick
        ),
        forceScroll = true
    )
}

@Preview(showBackground = true)
@Composable
fun GetReadyToSharePreview() {
    GdsTheme {
        GetReadyToShareContent(
            missingPrerequisites = listOf(Prerequisite.BLUETOOTH),
        )
    }
}
