package uk.gov.onelogin.sharing.holder.error.generic

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.coroutines.launch
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.logging.api.analytics.extensions.getEnglishString
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.putScreenState
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.rememberMetricsStateHolder
import uk.gov.onelogin.sharing.core.presentation.preview.PreviewContent
import uk.gov.onelogin.sharing.holder.R
import uk.gov.onelogin.sharing.holder.error.ErrorScreenTemplate
import uk.gov.onelogin.sharing.holder.error.ErrorScreenTemplateViewModel
import uk.gov.onelogin.sharing.holder.error.NavigationEvent
import uk.gov.onelogin.sharing.holder.error.createErrorBodyText

@Composable
internal fun GenericHolderErrorScreen(
    modifier: Modifier = Modifier,
    viewModel: GenericHolderErrorViewModel = metroViewModel(),
    errorTemplateViewModel: ErrorScreenTemplateViewModel = metroViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val navigationEvent by errorTemplateViewModel.navigationEvent.collectAsStateWithLifecycle(
        initialValue = null
    )

    val metrics = rememberMetricsStateHolder()
    LaunchedEffect(Unit) {
        metrics.putScreenState("GenericHolderErrorScreen")
    }

    LaunchedEffect(Unit) {
        scope.launch {
            R.string.holder_generic_error_title
                .let(context::getEnglishString)
                .let(viewModel::sendScreenEvent)
        }
    }

    GenericErrorScreenContent(
        modifier = modifier,
        navigationEvent = navigationEvent,
        onGoBackToDocumentsClick = errorTemplateViewModel::exit,
        onUpdateNavigationEvent = errorTemplateViewModel::sendButtonEvent
    )
}

@Composable
internal fun GenericErrorScreenContent(
    modifier: Modifier = Modifier,
    navigationEvent: NavigationEvent? = null,
    onGoBackToDocumentsClick: () -> Unit = {},
    onUpdateNavigationEvent: (String) -> Unit = {}
) {
    ErrorScreenTemplate(
        modifier = modifier,
        navigationEvent = navigationEvent,
        titleText = stringResource(R.string.holder_generic_error_title),
        body = { padding ->
            listOf(
                R.string.holder_generic_error_body_1,
                R.string.holder_generic_error_body_2
            ).forEach { resourceId ->
                createErrorBodyText(resourceId, padding)
            }
        },
        onGoBackToDocumentsClick = onGoBackToDocumentsClick,
        onUpdateNavigationEvent = onUpdateNavigationEvent
    )
}

@PreviewContent
@Composable
internal fun GenericHolderErrorScreenPreview() {
    GdsTheme {
        GenericErrorScreenContent(
            modifier = Modifier.fillMaxSize(),
            navigationEvent = null
        )
    }
}
