package uk.gov.onelogin.sharing.holder

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import uk.gov.onelogin.sharing.holder.engagement.EngagementGenerator
import uk.gov.onelogin.sharing.security.SessionSecurityImpl

private const val QR_SIZE = 800

@Composable
fun HolderWelcomeScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        HolderWelcomeText()

        val eDeviceKey = SessionSecurityImpl()
        eDeviceKey.generateEcPublicKey()

        val engagement = EngagementGenerator()
        QrCodeImage(
            modifier = Modifier,
            data = "mdoc:${engagement.qrCodeEngagement()}",
            size = QR_SIZE
        )
    }
}
