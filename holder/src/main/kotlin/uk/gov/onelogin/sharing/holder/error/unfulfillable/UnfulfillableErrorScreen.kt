package uk.gov.onelogin.sharing.holder.error.unfulfillable

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uk.gov.onelogin.sharing.holder.error.ErrorScreenTemplate

@Composable
fun UnfulfillableErrorScreen(
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
        titleText = "",
        onGoBackToDocumentsClick = onExitJourney
    )
}
