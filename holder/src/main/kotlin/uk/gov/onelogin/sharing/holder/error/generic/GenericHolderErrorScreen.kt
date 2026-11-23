package uk.gov.onelogin.sharing.holder.error.generic

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.logging.api.analytics.extensions.getEnglishString
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.putScreenState
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.rememberMetricsStateHolder
import uk.gov.onelogin.sharing.core.presentation.preview.PreviewContent
import uk.gov.onelogin.sharing.holder.R
import uk.gov.onelogin.sharing.holder.error.ErrorScreenTemplate
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
    val navigationEvent by viewModel.navigationEvent.collectAsStateWithLifecycle(
        initialValue = null
    )
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

    BackHandler(
        navigationEvent == null,
        buttonBehaviour
    )

    LaunchedEffect(navigationEvent) {
        when (navigationEvent) {
            is ViewModelEvent.ExitJourney -> {
                R.string.holder_generic_error_button
                    .let(context::getEnglishString)
                    .let(viewModel::sendButtonEvent)
                onExitJourney()
            }

            else -> {
                // do nothing with null events
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

    ErrorScreenTemplate(
        modifier = modifier,
        titleText = stringResource(R.string.holder_generic_error_title),
        body = { itemPadding ->
            listOf(
                R.string.holder_generic_error_body_1,
                R.string.holder_generic_error_body_2
            ).forEach { resourceId ->
                item {
                    Text(
                        stringResource(resourceId),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = itemPadding),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        },
        onGoBackToDocumentsClick = onGoBackToDocumentsClick
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
