package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.persistentListOf
import uk.gov.android.ui.patterns.leftalignedscreen.LeftAlignedScreenBodyV2
import uk.gov.android.ui.patterns.leftalignedscreen.LeftAlignedScreenButton
import uk.gov.android.ui.patterns.leftalignedscreen.LeftAlignedScreenV2
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.putScreenState
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.rememberMetricsStateHolder
import uk.gov.onelogin.sharing.holder.R
import uk.gov.onelogin.sharing.prerequisites.api.PrerequisiteAction
import uk.gov.onelogin.sharing.prerequisites.api.contracts.PrerequisiteActionContract

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

    TurnOnBluetoothContent(
        modifier = modifier,
        onTurnOnBluetooth = { viewModel.turnOnBluetooth(launcher) }
    )
}

@Composable
internal fun TurnOnBluetoothContent(
    modifier: Modifier = Modifier,
    onTurnOnBluetooth: () -> Unit
) {
    LeftAlignedScreenV2(
        modifier = modifier,
        title = stringResource(R.string.holder_turn_on_bluetooth),
        body = persistentListOf(
            LeftAlignedScreenBodyV2.Text(
                stringResource(R.string.holder_turn_on_bluetooth_body_1)
            ),
            LeftAlignedScreenBodyV2.Text(
                stringResource(R.string.holder_turn_on_bluetooth_body_2)
            )
        ),
        primaryButton = LeftAlignedScreenButton(
            text = stringResource(R.string.holder_turn_on_bluetooth),
            onClick = onTurnOnBluetooth
        ),
        forceScroll = false
    )
}
