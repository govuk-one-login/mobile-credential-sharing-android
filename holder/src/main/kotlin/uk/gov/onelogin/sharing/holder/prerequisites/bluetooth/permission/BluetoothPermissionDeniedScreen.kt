package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth.permission

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
import uk.gov.onelogin.sharing.prerequisites.api.PrerequisiteAction
import uk.gov.onelogin.sharing.prerequisites.api.contracts.PrerequisiteActionContract

internal const val BLUETOOTH_PERMISSION_DENIED_SPINNER_TEST_TAG = "bluetoothPermissionDeniedSpinner"

@Composable
internal fun BluetoothPermissionDeniedScreen(
    modifier: Modifier = Modifier,
    viewModel: BluetoothPermissionDeniedViewModel = metroViewModel(),
    onCancel: () -> Unit = {},
    launcher: ActivityResultLauncher<PrerequisiteAction> = rememberLauncherForActivityResult(
        PrerequisiteActionContract
    ) { viewModel.recheckPrerequisites() }
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    val hasPreviouslyRecheckedPrerequisites: Boolean by viewModel
        .hasRecheckedPrerequisites
        .collectAsStateWithLifecycle()

    val isLeaving: Boolean by viewModel.isClosing.collectAsStateWithLifecycle()

    val metrics = rememberMetricsStateHolder()
    LaunchedEffect(Unit) {
        metrics.putScreenState("BluetoothPermissionDeniedScreen")
        viewModel.sendScreenEvent(
            context.getEnglishString(R.string.holder_bluetooth_permission_denied_title)
        )
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
        BluetoothPermissionDeniedLoadingContent(modifier = modifier)
    } else {
        BluetoothPermissionDeniedContent(
            modifier = modifier,
            onAllow = {
                viewModel.sendAllowEvent(
                    context.getEnglishString(
                        R.string.holder_bluetooth_permission_denied_allow_button
                    )
                )
                viewModel.requestBluetoothPermission(launcher)
            },
            onCancel = {
                viewModel.sendCancelEvent(
                    context.getEnglishString(
                        R.string.holder_bluetooth_permission_denied_cancel_button
                    )
                )
                onCancel()
            }
        )
    }
}

@Suppress("LongMethod")
@Composable
internal fun BluetoothPermissionDeniedContent(
    onAllow: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    ErrorScreen(
        modifier = modifier.fillMaxWidth(),
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
                text = stringResource(R.string.holder_bluetooth_permission_denied_title),
                modifier = Modifier.padding(horizontal = horizontalPadding),
                textAlign = GdsHeadingAlignment.CenterAligned
            )
        },
        body = { horizontalPadding ->
            item {
                Text(
                    text = stringResource(R.string.holder_bluetooth_permission_denied_body_1),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding)
                )
            }
            item {
                Text(
                    text = stringResource(R.string.holder_bluetooth_permission_denied_body_2),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding)
                )
            }
        },
        supportingText = null,
        primaryButton = {
            GdsButton(
                text = stringResource(R.string.holder_bluetooth_permission_denied_allow_button),
                buttonType = ButtonTypeV2.Primary(),
                onClick = onAllow,
                modifier = Modifier.fillMaxWidth()
            )
        },
        secondaryButton = {
            GdsButton(
                text = stringResource(R.string.holder_bluetooth_permission_denied_cancel_button),
                buttonType = ButtonTypeV2.Secondary(),
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}

@Composable
internal fun BluetoothPermissionDeniedLoadingContent(
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
                modifier = Modifier.testTag(BLUETOOTH_PERMISSION_DENIED_SPINNER_TEST_TAG)
            )
        } else {
            CircularProgressIndicator(
                modifier = Modifier.testTag(BLUETOOTH_PERMISSION_DENIED_SPINNER_TEST_TAG)
            )
        }
    }
}

@PreviewContent
@Composable
internal fun BluetoothPermissionDeniedContentPreview() {
    GdsTheme {
        Surface {
            BluetoothPermissionDeniedContent(
                onAllow = {},
                onCancel = {},
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@PreviewContent
@Composable
internal fun BluetoothPermissionDeniedLoadingContentPreview() {
    GdsTheme {
        Surface {
            BluetoothPermissionDeniedLoadingContent(
                modifier = Modifier.fillMaxSize(),
                progress = { 0.7f }
            )
        }
    }
}
