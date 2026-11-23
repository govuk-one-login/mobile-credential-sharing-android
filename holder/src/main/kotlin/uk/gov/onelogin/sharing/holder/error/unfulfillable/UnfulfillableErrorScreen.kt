package uk.gov.onelogin.sharing.holder.error.unfulfillable

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.onelogin.sharing.core.presentation.preview.PreviewContent
import uk.gov.onelogin.sharing.holder.R
import uk.gov.onelogin.sharing.holder.error.ErrorScreenTemplate
import uk.gov.onelogin.sharing.holder.error.generic.GenericErrorScreenContent

@Composable
internal fun UnfulfillableErrorScreen(
    modifier: Modifier = Modifier,
) {
    val mainScope = rememberCoroutineScope { Dispatchers.Main }
    val onBackPressedDispatcherOwner = LocalOnBackPressedDispatcherOwner.current

    val onExitJourney: () -> Unit = {
        mainScope.launch {
            onBackPressedDispatcherOwner?.onBackPressedDispatcher?.onBackPressed()
        }
    }

    ErrorScreenTemplate(
        modifier = modifier,
        titleText = stringResource(R.string.holder_unfulfillable_error_title),
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
