package uk.gov.onelogin.sharing.holder.qrcode.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import uk.gov.android.ui.theme.spacingDouble
import uk.gov.onelogin.sharing.holder.QrCodeImage
import uk.gov.onelogin.sharing.holder.R

private const val QR_SIZE = 800

/**
 * The QR code inside a white box (temporary, matching iOS).
 *
 * The box is white in light and dark mode and acts as the QR's quiet zone. It's hidden while the
 * QR refreshes, leaving a blank space of the same size.
 *
 * @param horizontalPadding White space is either side of the QR. In portrait this is the screen's
 * content padding., so the QR lines up with the heading and body while the box reaches the edges.
 * @param maxQrSize Caps the QR so it fits on short screens.
 */
@Composable
internal fun QrCodeScreenImage(
    qrPayload: String?,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = spacingDouble,
    maxQrSize: Dp = Dp.Unspecified
) {
    Box(
        modifier = modifier
            .then(if (qrPayload != null) Modifier.background(Color.White) else Modifier)
            .padding(horizontal = horizontalPadding, vertical = spacingDouble),
        contentAlignment = Alignment.CenterStart
    ) {
        val squareModifier = Modifier
            .widthIn(max = maxQrSize)
            .fillMaxWidth()
            .aspectRatio(1f)

        if (qrPayload == null) {
            Box(modifier = squareModifier)
        } else {
            QrCodeImage(
                data = qrPayload,
                size = QR_SIZE,
                margin = 0,
                modifier = squareModifier,
                contentDescription = stringResource(R.string.holder_qr_code_alt_text)
            )
        }
    }
}
