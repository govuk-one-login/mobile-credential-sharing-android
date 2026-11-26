package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth.denied

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import uk.gov.android.ui.componentsv2.button.ButtonTypeV2
import uk.gov.android.ui.componentsv2.button.GdsButton
import uk.gov.android.ui.componentsv2.heading.GdsHeading
import uk.gov.android.ui.componentsv2.heading.GdsHeadingAlignment
import uk.gov.android.ui.componentsv2.images.GdsIcon
import uk.gov.android.ui.patterns.errorscreen.v2.ErrorScreen
import uk.gov.android.ui.patterns.errorscreen.v2.ErrorScreenIcon
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.logging.api.analytics.extensions.getEnglishString
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.putScreenState
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.rememberMetricsStateHolder
import uk.gov.onelogin.sharing.core.presentation.preview.PreviewContent
import uk.gov.onelogin.sharing.holder.R
import uk.gov.onelogin.sharing.holder.prerequisites.bluetooth.LOADING_SPINNER_TEST_TAG
import uk.gov.onelogin.sharing.prerequisites.api.PrerequisiteAction
import uk.gov.onelogin.sharing.prerequisites.api.contracts.PrerequisiteActionContract

@Composable
@Suppress("LongMethod")
internal fun DeviceBleDeniedScreen(
    modifier: Modifier = Modifier,
    viewModel: DeviceBleDeniedViewModel = metroViewModel(),
    launcher: ActivityResultLauncher<PrerequisiteAction> = rememberLauncherForActivityResult(
        PrerequisiteActionContract
    ) {}
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    val isLeaving: Boolean by viewModel.isClosing.collectAsStateWithLifecycle()

    val metrics = rememberMetricsStateHolder()
    LaunchedEffect(Unit) {
        metrics.putScreenState("DeviceBleDeniedScreen")
        viewModel.sendScreenEvent(
            context.getEnglishString(R.string.holder_device_ble_denied_title)
        )
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> viewModel.onScreenPaused()
                Lifecycle.Event.ON_RESUME -> viewModel.onScreenResumed()
                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val onCancel: () -> Unit = {
        viewModel.sendCancelEvent(
            context.getEnglishString(R.string.holder_device_ble_denied_cancel_button)
        )
        viewModel.exitJourney()
    }

    // Disabled once leaving, so a second back press can't cancel the journey twice.
    BackHandler(enabled = !isLeaving, onBack = onCancel)

    if (isLeaving) {
        DeviceBleDeniedLoadingContent(modifier = modifier)
    } else {
        DeviceBleDeniedContent(
            modifier = modifier,
            onTurnOnBluetooth = {
                viewModel.sendTurnOnBluetoothEvent(
                    context.getEnglishString(R.string.holder_device_ble_denied_turn_on_button)
                )
                viewModel.turnOnBluetooth(launcher)
            },
            onCancel = onCancel
        )
    }
}

@Composable
internal fun DeviceBleDeniedContent(
    onTurnOnBluetooth: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    ErrorScreen(
        modifier = modifier,
        icon = { horizontalPadding ->
            GdsIcon(
                image = ImageVector.vectorResource(ErrorScreenIcon.ErrorIcon.icon),
                contentDescription = stringResource(ErrorScreenIcon.ErrorIcon.description),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding),
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        title = { horizontalPadding ->
            GdsHeading(
                text = stringResource(R.string.holder_device_ble_denied_title),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding),
                textAlign = GdsHeadingAlignment.CenterAligned
            )
        },
        body = { horizontalPadding ->
            listOf(
                R.string.holder_device_ble_denied_body_1,
                R.string.holder_device_ble_denied_body_2
            ).forEach { resourceId ->
                item {
                    Text(
                        text = stringResource(resourceId),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = horizontalPadding),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        },
        supportingText = null,
        primaryButton = {
            GdsButton(
                text = stringResource(R.string.holder_device_ble_denied_turn_on_button),
                buttonType = ButtonTypeV2.Primary(),
                onClick = onTurnOnBluetooth,
                modifier = Modifier.fillMaxWidth()
            )
        },
        secondaryButton = {
            GdsButton(
                text = stringResource(R.string.holder_device_ble_denied_cancel_button),
                buttonType = ButtonTypeV2.Secondary(),
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}

@Composable
internal fun DeviceBleDeniedLoadingContent(
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
internal fun DeviceBleDeniedContentPreview() {
    GdsTheme {
        Surface {
            DeviceBleDeniedContent(
                onTurnOnBluetooth = {},
                onCancel = {},
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@PreviewContent
@Composable
internal fun DeviceBleDeniedLoadingContentPreview() {
    GdsTheme {
        Surface {
            DeviceBleDeniedLoadingContent(
                modifier = Modifier.fillMaxSize(),
                progress = { 0.7f }
            )
        }
    }
}
