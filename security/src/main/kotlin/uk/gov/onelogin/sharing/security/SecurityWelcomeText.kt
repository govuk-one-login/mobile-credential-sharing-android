package uk.gov.onelogin.sharing.security

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SecurityWelcomeText(modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = "Welcome to GOV.UK Wallet Sharing's security module!"
    )
}
