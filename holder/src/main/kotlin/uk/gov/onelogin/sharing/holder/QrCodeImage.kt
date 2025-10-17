package uk.gov.onelogin.sharing.holder

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale

@Composable
fun QrCodeImage(
    modifier: Modifier = Modifier,
    data: String,
    size: Int
) {
    qrCodeGenerator(data, size)?.asImageBitmap()?.let {
        Image(
            modifier = modifier,
            bitmap = it,
            contentDescription = "QR code to gov.uk",
            contentScale = ContentScale.Fit
        )
    }
}