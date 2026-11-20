package uk.gov.onelogin.sharing.holder

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale

@Composable
fun QrCodeImage(
    data: String,
    size: Int,
    modifier: Modifier = Modifier,
    margin: Int = DEFAULT_QR_MARGIN,
    contentDescription: String = "QR Code"
) {
    val bitmap = remember(data, size, margin) {
        qrCodeGenerator(data, size, margin)?.asImageBitmap()
    }

    bitmap?.let {
        Image(
            modifier = modifier,
            bitmap = it,
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit
        )
    }
}
