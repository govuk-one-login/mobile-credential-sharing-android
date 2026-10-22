package uk.gov.onelogin.sharing.holder.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import uk.gov.onelogin.sharing.holder.QrCodeImage
import uk.gov.onelogin.sharing.holder.R

private const val QR_SIZE = 800
private const val QR_MARGIN = 0

@Composable
internal fun HolderWelcomeQrCode(qrData: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        QrCodeImage(
            data = qrData,
            size = QR_SIZE,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            margin = QR_MARGIN,
            contentDescription = stringResource(R.string.holder_welcome_qr_code_alt_text)
        )
    }
}
