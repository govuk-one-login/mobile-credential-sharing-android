package uk.gov.onelogin.sharing.holder.error.unfulfillable

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
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
internal fun UnfulfillableErrorScreen(
    modifier: Modifier = Modifier,
    viewModel: UnfulfillableErrorViewModel = metroViewModel(),
    errorTemplateViewModel: ErrorScreenTemplateViewModel = metroViewModel()
) {
    val navigationEvent by errorTemplateViewModel.navigationEvent.collectAsStateWithLifecycle(
        initialValue = null
    )

    UnfulfillableErrorScreenContent(
        modifier = modifier,
        navigationEvent = navigationEvent,
        onLaunch = viewModel::sendScreenEvent,
        onGoBackToDocumentsClick = errorTemplateViewModel::reset,
        onUpdateNavigationEvent = errorTemplateViewModel::sendButtonEvent
    )
}

@Composable
internal fun UnfulfillableErrorScreenContent(
    modifier: Modifier = Modifier,
    navigationEvent: NavigationEvent? = null,
    onLaunch: (String) -> Unit = {},
    onGoBackToDocumentsClick: () -> Unit = {},
    onUpdateNavigationEvent: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val latestOnLaunch by rememberUpdatedState(onLaunch)
    val metrics = rememberMetricsStateHolder()
    LaunchedEffect(Unit) {
        metrics.putScreenState("UnfulfillableErrorScreen")
    }

    val titleRes = R.string.holder_unfulfillable_error_title
    LaunchedEffect(Unit) {
        scope.launch {
            titleRes
                .let(context::getEnglishString)
                .let(latestOnLaunch)
        }
    }

    ErrorScreenTemplate(
        modifier = modifier,
        navigationEvent = navigationEvent,
        titleText = stringResource(titleRes),
        body = { padding ->
            listOf(
                R.string.holder_unfulfillable_error_body_1,
                R.string.holder_unfulfillable_error_body_2
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
internal fun UnfulfillableErrorScreenPreview() {
    GdsTheme {
        UnfulfillableErrorScreenContent(
            modifier = Modifier.fillMaxSize(),
            navigationEvent = null,
        )
    }
}
