package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.persistentListOf
import uk.gov.android.ui.patterns.leftalignedscreen.LeftAlignedScreenBodyV2
import uk.gov.android.ui.patterns.leftalignedscreen.LeftAlignedScreenButton
import uk.gov.android.ui.patterns.leftalignedscreen.LeftAlignedScreenV2
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.putScreenState
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.rememberMetricsStateHolder
import uk.gov.onelogin.sharing.core.presentation.preview.PreviewContent
import uk.gov.onelogin.sharing.holder.R
import uk.gov.onelogin.sharing.prerequisites.api.PrerequisiteAction
import uk.gov.onelogin.sharing.prerequisites.api.contracts.PrerequisiteActionContract

internal const val LOADING_SPINNER_TEST_TAG = "loadingSpinner"

@Composable
internal fun TurnOnBluetoothScreen(
    modifier: Modifier = Modifier,
    viewModel: TurnOnBluetoothViewModel = metroViewModel(),
    launcher: ActivityResultLauncher<PrerequisiteAction> = rememberLauncherForActivityResult(
        PrerequisiteActionContract
    ) { viewModel.recheckPrerequisites() }
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    val hasPreviouslyRecheckedPrerequisites: Boolean by viewModel
        .hasRecheckedPrerequisites
        .collectAsStateWithLifecycle()

    val isLeaving: Boolean by viewModel.isClosing.collectAsStateWithLifecycle()

    val metrics = rememberMetricsStateHolder()
    LaunchedEffect(Unit) {
        metrics.putScreenState("TurnOnBluetoothScreen")
        viewModel.sendScreenEvent()
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

    if (isLeaving) {
        TurnOnBluetoothLoadingContent(modifier = modifier)
    } else {
        TurnOnBluetoothContent(
            modifier = modifier,
            onTurnOnBluetooth = { viewModel.turnOnBluetooth(launcher) }
        )
    }
}

@Composable
internal fun TurnOnBluetoothContent(onTurnOnBluetooth: () -> Unit, modifier: Modifier = Modifier) {
    LeftAlignedScreenV2(
        modifier = modifier,
        title = stringResource(R.string.holder_turn_on_bluetooth_title),
        body = persistentListOf(
            LeftAlignedScreenBodyV2.Text(
                stringResource(R.string.holder_turn_on_bluetooth_body_1)
            ),
            LeftAlignedScreenBodyV2.Text(
                stringResource(R.string.holder_turn_on_bluetooth_body_2)
            )
        ),
        primaryButton = LeftAlignedScreenButton(
            text = stringResource(R.string.holder_turn_on_bluetooth_button),
            onClick = onTurnOnBluetooth
        ),
        forceScroll = false
    )
}

@Composable
internal fun TurnOnBluetoothLoadingContent(
    modifier: Modifier = Modifier,
    progress: (() -> Float)? = null
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (progress != null) {
            CircularProgressIndicator(
                progress = progress,
                modifier = Modifier.testTag(LOADING_SPINNER_TEST_TAG)
            )
        } else {
            CircularProgressIndicator(modifier = Modifier.testTag(LOADING_SPINNER_TEST_TAG))
        }
    }
}

@PreviewContent
@Composable
internal fun TurnOnBluetoothContentPreview() {
    GdsTheme {
        Surface {
            TurnOnBluetoothContent(
                onTurnOnBluetooth = {},
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@PreviewContent
@Composable
internal fun TurnOnBluetoothLoadingContentPreview() {
    GdsTheme {
        Surface {
            TurnOnBluetoothLoadingContent(
                modifier = Modifier.fillMaxSize(),
                progress = { 0.7f }
            )
        }
    }
}
