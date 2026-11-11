package uk.gov.onelogin.sharing.holder.error.generic

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
import uk.gov.onelogin.sharing.holder.error.generic.GenericHolderErrorViewModel.NavigationEvent as ViewModelEvent

@Composable
internal fun GenericHolderErrorScreen(
    modifier: Modifier = Modifier,
    viewModel: GenericHolderErrorViewModel = metroViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val onBackPressedDispatcherOwner = LocalOnBackPressedDispatcherOwner.current
    val mainScope = rememberCoroutineScope { Dispatchers.Main }
    val onExitJourney: () -> Unit = {
        mainScope.launch {
            onBackPressedDispatcherOwner?.onBackPressedDispatcher?.onBackPressed()
        }
    }

    val metrics = rememberMetricsStateHolder()
    LaunchedEffect(Unit) {
        metrics.putScreenState("GenericHolderErrorScreen")
    }

    val buttonBehaviour: () -> Unit = {
        scope.launch {
            R.string.holder_generic_error_button
                .let(context::getEnglishString)
                .let(viewModel::sendButtonEvent)
            viewModel.reset()
        }
    }

    LaunchedEffect(Unit) {
        scope.launch {
            R.string.holder_generic_error_title
                .let(context::getEnglishString)
                .let(viewModel::sendScreenEvent)
        }
    }

    BackHandler(true, buttonBehaviour)

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is ViewModelEvent.ExitJourney -> {
                    onExitJourney()
                }

                else -> {
                    // do nothing with null events
                }
            }
        }
    }

    GenericErrorScreenContent(modifier) {
        buttonBehaviour()
    }
}

@Composable
internal fun GenericErrorScreenContent(
    modifier: Modifier = Modifier,
    onGoBackToDocumentsClick: () -> Unit = {}
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
                text = stringResource(R.string.holder_generic_error_title),
                modifier = Modifier
                    .padding(horizontal = horizontalPadding),
                textAlign = GdsHeadingAlignment.CenterAligned
            )
        },
        body = { itemPadding ->
            listOf(
                R.string.holder_generic_error_body_1,
                R.string.holder_generic_error_body_2
            ).forEach { resourceId ->
                item {
                    Text(
                        stringResource(resourceId),
                        modifier = Modifier.padding(horizontal = itemPadding),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
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
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}

@PreviewContent
@Composable
internal fun GenericHolderErrorScreenPreview() {
    GdsTheme {
        GenericErrorScreenContent(
            modifier = Modifier.fillMaxSize()
        )
    }
}
