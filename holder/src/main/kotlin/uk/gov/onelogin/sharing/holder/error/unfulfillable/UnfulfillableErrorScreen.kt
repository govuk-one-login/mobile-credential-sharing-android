package uk.gov.onelogin.sharing.holder.error.unfulfillable

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import uk.gov.onelogin.sharing.holder.error.ErrorScreenTemplate

@Composable
fun UnfulfillableErrorScreen(
    modifier: Modifier = Modifier,
    onGoBackToDocumentsClick: () -> Unit = {}
) {
    ErrorScreenTemplate(
        titleText = "",
    )
}
