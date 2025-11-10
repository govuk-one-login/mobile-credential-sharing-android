package uk.gov.onelogin.sharing.holder.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import uk.gov.onelogin.sharing.holder.QrCodeImage
import uk.gov.onelogin.sharing.holder.engagement.EngagementAlgorithms.EC_ALGORITHM
import uk.gov.onelogin.sharing.holder.engagement.EngagementAlgorithms.EC_PARAMETER_SPEC
import uk.gov.onelogin.sharing.holder.engagement.EngagementGenerator
import uk.gov.onelogin.sharing.security.cose.CoseKey
import uk.gov.onelogin.sharing.security.secureArea.SessionSecurityImpl

private const val QR_SIZE = 800

@Composable
fun HolderWelcomeScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        HolderWelcomeText()

        val eDeviceKey = SessionSecurityImpl()
        val ecPublicKey = eDeviceKey.generateEcPublicKey(EC_ALGORITHM, EC_PARAMETER_SPEC)
        val engagement = EngagementGenerator()

        ecPublicKey?.let {
            val key = CoseKey.generateCoseKey(it)
            println("Successfully created CoseKey: $key")

            QrCodeImage(
                modifier = Modifier,
                data = "mdoc:${engagement.qrCodeEngagement(key)}",
                size = QR_SIZE
            )
        }
    }
}
