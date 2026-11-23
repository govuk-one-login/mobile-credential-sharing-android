package uk.gov.onelogin.sharing.holder.error.unfulfillable

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.logging.api.analytics.extensions.getEnglishString
import uk.gov.onelogin.sharing.core.presentation.preview.PreviewContent
import uk.gov.onelogin.sharing.holder.R
import uk.gov.onelogin.sharing.holder.error.ErrorScreenTemplate
import uk.gov.onelogin.sharing.holder.error.createErrorBodyText

@Composable
internal fun UnfulfillableErrorScreen(
    modifier: Modifier = Modifier,
    viewModel: UnfulfillableErrorViewModel = metroViewModel(),
) {
    val context = LocalContext.current
    val mainScope = rememberCoroutineScope { Dispatchers.Main }
    val onBackPressedDispatcherOwner = LocalOnBackPressedDispatcherOwner.current
    val scope = rememberCoroutineScope()

    val onExitJourney: () -> Unit = {
        mainScope.launch {
            onBackPressedDispatcherOwner?.onBackPressedDispatcher?.onBackPressed()
        }
    }

    val titleRes = R.string.holder_unfulfillable_error_title
    LaunchedEffect(Unit) {
        scope.launch {
            viewModel.sendScreenEvent(context.getEnglishString(titleRes)).join()
        }
    }

    ErrorScreenTemplate(
        modifier = modifier,
        titleText = stringResource(titleRes),
        body = { padding ->
            listOf(
                R.string.holder_unfulfillable_error_body_1,
                R.string.holder_unfulfillable_error_body_2
            ).forEach { resourceId ->
                createErrorBodyText(resourceId, padding)
            }
        },
        onGoBackToDocumentsClick = onExitJourney
    )
}

@PreviewContent
@Composable
internal fun GenericHolderErrorScreenPreview() {
    GdsTheme {
        UnfulfillableErrorScreen(
            modifier = Modifier.fillMaxSize()
        )
    }
}
