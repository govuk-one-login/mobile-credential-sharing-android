package uk.gov.onelogin.sharing.holder.getreadytoshare

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewDynamicColors
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch
import uk.gov.android.ui.patterns.leftalignedscreen.LeftAlignedScreenBodyV2
import uk.gov.android.ui.patterns.leftalignedscreen.LeftAlignedScreenButton
import uk.gov.android.ui.patterns.leftalignedscreen.LeftAlignedScreenV2
import uk.gov.android.ui.theme.m3.GdsLocalColorScheme
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.onelogin.sharing.core.WalletSharingPreview
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.putScreenState
import uk.gov.onelogin.sharing.holder.R
import uk.gov.onelogin.sharing.prerequisites.api.PrerequisiteAction
import uk.gov.onelogin.sharing.prerequisites.api.contracts.PrerequisiteActionContract

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
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
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

    LaunchedEffect(Unit) {
        scope.launch {
            viewModel.sendScreenEvent()
        }
    }

    GetReadyToShareContent(
        modifier = modifier,
        onButtonClick = { viewModel.resolve(launcher) },
        onFindOutMoreButtonClick = viewModel::onFindOutMore
    )
}

@Composable
internal fun GetReadyToShareContent(
    modifier: Modifier = Modifier,
    onButtonClick: () -> Unit = {},
    onFindOutMoreButtonClick: () -> Unit = {},
) {
    LeftAlignedScreenV2(
        modifier = modifier,
        title = stringResource(R.string.get_ready_to_share_header),
        body = listOf(
            R.string.get_ready_to_share_before_sharing,
            R.string.get_ready_to_share_if_bluetooth_is_off,
            R.string.get_ready_to_share_use_qr_to_connect,
        ).map {
            stringResource(it)
        }.map {
            LeftAlignedScreenBodyV2.Text(text = it)
        }.toPersistentList(),
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

@WalletSharingPreview
@Composable
internal fun GetReadyToSharePreview() {
    GdsTheme {
        GetReadyToShareContent(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .safeContentPadding()
        )
    }
}
