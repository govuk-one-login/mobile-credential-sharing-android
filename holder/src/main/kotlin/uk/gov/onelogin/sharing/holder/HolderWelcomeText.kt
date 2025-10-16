package uk.gov.onelogin.sharing.holder

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun HolderWelcomeText(
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier,
        text = "Welcome to GOV.UK Wallet Sharing"
    )
}
