package uk.gov.onelogin.sharing.holder.qrcode.component

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import uk.gov.android.ui.patterns.leftalignedscreen.LeftAlignedScreen
import uk.gov.android.ui.theme.spacingQuadruple

@Composable
internal fun PortraitQrContent(qrPayload: String?, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier) {
        // Whole QR code must fit in the viewport
        val maxQrSize = maxHeight - spacingQuadruple * 2

        LeftAlignedScreen(
            modifier = Modifier.fillMaxSize(),
            title = { padding ->
                QrCodeScreenHeading(Modifier.padding(horizontal = padding))
            },
            body = { padding ->
                item {
                    QrCodeScreenImage(
                        qrPayload = qrPayload,
                        modifier = Modifier.fillMaxWidth(),
                        horizontalPadding = padding,
                        maxQrSize = maxQrSize
                    )
                }
                item {
                    QrCodeScreenBody(Modifier.padding(horizontal = padding))
                }
            }
        )
    }
}
