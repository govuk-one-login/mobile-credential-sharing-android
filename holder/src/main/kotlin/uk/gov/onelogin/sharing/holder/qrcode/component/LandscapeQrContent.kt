package uk.gov.onelogin.sharing.holder.qrcode.component

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import uk.gov.android.ui.patterns.leftalignedscreen.LeftAlignedScreen
import uk.gov.android.ui.theme.spacingDouble
import uk.gov.android.ui.theme.spacingQuadruple

private val LANDSCAPE_QR_SIZE = 250.dp

@Composable
internal fun LandscapeQrContent(qrPayload: String?, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.padding(
            start = spacingDouble + spacingQuadruple,
            end = spacingDouble + spacingQuadruple
        )
    ) {
        BoxWithConstraints {
            val qrSize = min(LANDSCAPE_QR_SIZE, maxHeight - spacingQuadruple)

            Row {
                LeftAlignedScreen(
                    modifier = Modifier.weight(1f),
                    title = {
                        QrCodeScreenHeading(
                            modifier = Modifier.padding(end = spacingQuadruple)
                        )
                    },
                    body = {
                        item {
                            QrCodeScreenBody(
                                modifier = Modifier.padding(end = spacingQuadruple)
                            )
                        }
                    }
                )
                QrCodeScreenImage(
                    qrPayload = qrPayload,
                    modifier = Modifier
                        .padding(start = spacingDouble, top = spacingDouble, end = spacingDouble)
                        .size(qrSize)
                )
            }
        }
    }
}
