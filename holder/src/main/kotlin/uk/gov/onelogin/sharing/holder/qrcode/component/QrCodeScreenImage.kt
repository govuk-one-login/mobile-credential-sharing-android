package uk.gov.onelogin.sharing.holder.qrcode.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import uk.gov.android.ui.theme.m3.toMappedColors
import uk.gov.onelogin.sharing.core.presentation.theme.Borders.sharingQrCode
import uk.gov.onelogin.sharing.holder.QrCodeImage
import uk.gov.onelogin.sharing.holder.R

private const val QR_SIZE = 800
private val QR_BORDER_SIZE = 1.dp
private val QR_BORDER_SHAPE = RoundedCornerShape(8.dp)

/**
 * The QR code and its quiet zone inside a bordered container. The border lines up with the heading
 * and body components. While the QR refreshes, the slot is left blank.
 */
@Composable
internal fun QrCodeScreenImage(qrPayload: String?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val squareModifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)

        if (qrPayload == null) {
            Box(modifier = squareModifier)
        } else {
            QrCodeImage(
                data = qrPayload,
                size = QR_SIZE,
                modifier = squareModifier
                    .clip(QR_BORDER_SHAPE)
                    .border(
                        width = QR_BORDER_SIZE,
                        color = sharingQrCode.toMappedColors(),
                        shape = QR_BORDER_SHAPE
                    ),
                contentDescription = stringResource(R.string.holder_qr_code_alt_text)
            )
        }
    }
}
