package uk.gov.onelogin.sharing.holder

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import uk.gov.onelogin.sharing.holder.engagement.EngagementGenerator

private const val QR_SIZE = 800

@Composable
fun HolderWelcomeScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        HolderWelcomeText()
        val engagement = EngagementGenerator()
        QrCodeImage(
            modifier = Modifier,
            data = "mdoc://${engagement.qrCodeEngagement()}",
            size = QR_SIZE
        )
    }
}

@Composable
fun HolderWelcomeText(modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = "Welcome to GOV.UK Wallet Sharing"
    )
}
