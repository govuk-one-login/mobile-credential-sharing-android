package uk.gov.onelogin.sharing.holder.awaitingresolution

import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import dev.zacsweers.metrox.viewmodel.metroViewModel
import uk.gov.android.ui.componentsv2.button.ButtonTypeV2
import uk.gov.android.ui.componentsv2.button.GdsButton
import uk.gov.android.ui.componentsv2.heading.GdsHeading
import uk.gov.android.ui.componentsv2.heading.GdsHeadingAlignment
import uk.gov.android.ui.componentsv2.images.GdsIcon
import uk.gov.android.ui.patterns.centrealignedscreen.CentreAlignedScreen
import uk.gov.android.ui.theme.m3.GdsLocalColorScheme
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.logging.api.analytics.extensions.getEnglishString
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.putScreenState
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.rememberMetricsStateHolder
import uk.gov.onelogin.sharing.core.presentation.preview.PreviewContent
import uk.gov.onelogin.sharing.holder.R

@Composable
internal fun AwaitingVerifierResolutionScreen(
    modifier: Modifier = Modifier,
    viewModel: AwaitingVerifierResolutionViewModel = metroViewModel(),
    onExitJourney: () -> Unit = {}
) {
    val context = LocalContext.current
    val currentOnExitJourney by rememberUpdatedState(onExitJourney)

    /**
     * Self-disabling callback handles both system/hardware back and "Go back to documents" so the
     * journey is reset exactly once before control is handed back to the host app.
     *
     * Replaces [androidx.activity.compose.BackHandler]: `onExitJourney` re-dispatches the back
     * press to the host app, which a BackHandler catches and causes recursion until a
     * StackOverflowError.
     *
     * Using this approach allows us to disable the callback before `onExitJourney`,
     * allowing the back press to reach the host app.
     */
    val exitCallback = remember {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!isEnabled) return
                isEnabled = false
                viewModel.reset()
                currentOnExitJourney()
            }
        }
    }

    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(backDispatcher, lifecycleOwner) {
        backDispatcher?.addCallback(lifecycleOwner, exitCallback)
        onDispose { exitCallback.remove() }
    }

    val metrics = rememberMetricsStateHolder()
    val titleRes = R.string.holder_awaiting_verifier_resolution_title
    LaunchedEffect(Unit) {
        metrics.putScreenState("AwaitingVerifierResolutionScreen")
        viewModel.sendScreenEvent(context.getEnglishString(titleRes))
    }

    AwaitingVerifierResolutionContent(
        modifier = modifier,
        onGoBackToDocuments = {
            viewModel.sendGoBackToDocumentsEvent()
            exitCallback.handleOnBackPressed()
        }
    )
}

@Suppress("LongMethod")
@Composable
internal fun AwaitingVerifierResolutionContent(
    modifier: Modifier = Modifier,
    onGoBackToDocuments: () -> Unit = {}
) {
    CentreAlignedScreen(
        title = { padding ->
            GdsHeading(
                text = stringResource(R.string.holder_awaiting_verifier_resolution_title),
                textAlign = GdsHeadingAlignment.CenterAligned,
                modifier = Modifier.padding(horizontal = padding)
            )
        },
        modifier = modifier.fillMaxSize(),
        image = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = padding),
                contentAlignment = Alignment.Center
            ) {
                GdsIcon(
                    image = ImageVector.vectorResource(R.drawable.check_circle),
                    contentDescription = "Success",
                    modifier = Modifier.size(75.dp),
                    color = GdsLocalColorScheme.current.successIcon
                )
            }
        },
        body = { padding ->
            item {
                Text(
                    text = stringResource(R.string.holder_awaiting_verifier_resolution_body_1),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 17.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = padding)
                )
            }
            item {
                Text(
                    text = stringResource(R.string.holder_awaiting_verifier_resolution_body_2),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 17.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = padding)
                )
            }
        },
        primaryButton = {
            GdsButton(
                text = stringResource(R.string.holder_awaiting_verifier_resolution_button),
                buttonType = ButtonTypeV2.Primary(),
                onClick = onGoBackToDocuments,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}

@Composable
@PreviewContent
internal fun AwaitingVerifierResolutionScreenPreview() {
    GdsTheme {
        Surface {
            AwaitingVerifierResolutionContent(
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
