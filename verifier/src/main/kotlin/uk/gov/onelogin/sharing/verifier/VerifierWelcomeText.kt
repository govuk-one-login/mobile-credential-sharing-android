package uk.gov.onelogin.sharing.verifier

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun VerifierWelcomeText(
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier,
        text = "Welcome to GOV.UK Wallet Sharing"
    )
}
