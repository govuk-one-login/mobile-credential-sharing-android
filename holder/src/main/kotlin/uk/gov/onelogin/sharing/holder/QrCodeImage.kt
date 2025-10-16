package uk.gov.onelogin.sharing.holder

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale

@Composable
fun QrCodeImage(
    modifier: Modifier = Modifier,
    data: String, size: Int
) {
    val qr = qrCodeGenerator(data, size)

    if (qr != null) {
        Image(
            bitmap = qr.asImageBitmap(),
            contentDescription = "QR code to gov.uk",
            contentScale = ContentScale.Fit
        )
    }
}