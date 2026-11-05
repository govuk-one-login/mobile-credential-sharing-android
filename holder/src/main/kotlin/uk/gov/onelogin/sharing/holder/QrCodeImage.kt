package uk.gov.onelogin.sharing.holder

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale

@Composable
fun QrCodeImage(data: String, size: Int, modifier: Modifier = Modifier) {
    val bitmap = remember(data, size) {
        qrCodeGenerator(data, size)?.asImageBitmap()
    }

    bitmap?.let {
        Image(
            modifier = modifier,
            bitmap = it,
            contentDescription = "QR Code",
            contentScale = ContentScale.Fit
        )
    }
}
