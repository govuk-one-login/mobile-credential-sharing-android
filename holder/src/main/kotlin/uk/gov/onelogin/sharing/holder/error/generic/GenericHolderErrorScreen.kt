package uk.gov.onelogin.sharing.holder.error.generic

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.coroutines.launch
import uk.gov.android.ui.componentsv2.button.ButtonTypeV2
import uk.gov.android.ui.componentsv2.button.GdsButton
import uk.gov.android.ui.componentsv2.heading.GdsHeading
import uk.gov.android.ui.componentsv2.heading.GdsHeadingAlignment
import uk.gov.android.ui.componentsv2.images.GdsIcon
import uk.gov.android.ui.componentsv2.supportingtext.GdsSupportingText
import uk.gov.android.ui.patterns.errorscreen.v2.ErrorScreen
import uk.gov.android.ui.patterns.errorscreen.v2.ErrorScreenBodyContent
import uk.gov.android.ui.patterns.errorscreen.v2.ErrorScreenIcon
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.onelogin.sharing.core.R as coreR
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.putScreenState
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.rememberMetricsStateHolder
import uk.gov.onelogin.sharing.core.presentation.preview.PreviewContent
import uk.gov.onelogin.sharing.holder.R
import uk.gov.onelogin.sharing.holder.error.generic.GenericHolderViewModel.NavigationEvent as ViewModelEvent

@Composable
internal fun GenericHolderErrorScreen(
    modifier: Modifier = Modifier,
    viewModel: GenericHolderViewModel = metroViewModel(),
    onExitJourney: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val currentOnExitJourney by rememberUpdatedState(onExitJourney)

    val metrics = rememberMetricsStateHolder()
    LaunchedEffect(Unit) {
        metrics.putScreenState("UnrecoverableHolderErrorScreen")
    }

    BackHandler(true) {
        scope.launch { viewModel.reset() }
    }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is ViewModelEvent.ExitJourney -> {
                    currentOnExitJourney()
                }

                else -> {
                    // do nothing with null events
                }
            }
        }
    }

    GenericErrorScreenContent(
        modifier,
    ) {
        scope.launch {
            viewModel.reset()
        }
    }
}

@Composable
private fun GenericErrorScreenContent(
    modifier: Modifier,
    onGoBackToDocumentsClick: () -> Unit = {},
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
                color = colorScheme.onBackground
            )
        },
        title = { horizontalPadding ->
            GdsHeading(
                text = "Sharing is currently unavailable",
                modifier = Modifier
                    .padding(horizontal = horizontalPadding),
                textAlign = GdsHeadingAlignment.CenterAligned
            )
        },
        body = { itemPadding ->
            listOf(
                R.string.holder_generic_error_body_1,
                R.string.holder_generic_error_body_2,
            ).forEach { resourceId ->
                item {
                    Text(
                        stringResource(resourceId),
                        modifier = Modifier.padding(horizontal = itemPadding),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        },
        supportingText = null,
        primaryButton = {
            GdsButton(
                text = stringResource(R.string.holder_generic_error_button),
                buttonType = ButtonTypeV2.Primary(),
                onClick = onGoBackToDocumentsClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    )
}

@PreviewContent
@Composable
fun GenericHolderErrorScreenPreview() {
    GdsTheme {
        GenericErrorScreenContent(
            modifier = Modifier.fillMaxSize()
        )
    }
}
