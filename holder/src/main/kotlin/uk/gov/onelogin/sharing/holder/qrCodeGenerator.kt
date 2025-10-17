package uk.gov.onelogin.sharing.holder

import android.graphics.Bitmap
import androidx.core.graphics.createBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

/**
 * Generates a QR code bitmap from the given string data.
 *
 * @param data The string to be encoded into the QR code.
 * @param size The desired width and height of the QR code in pixels.
 * @return A [Bitmap] object representing the QR code, or `null` if an error occurs during
 * generation.
 */

fun qrCodeGenerator(data: String, size: Int): Bitmap? = try {
    val writer = QRCodeWriter()
    val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, size, size)
    val width = bitMatrix.width
    val height = bitMatrix.height
    val pixels = IntArray(width * height)
    for (y in 0 until height) {
        val offset = y * width
        for (x in 0 until width) {
            pixels[offset + x] =
                if (bitMatrix.get(x, y)) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
        }
    }
    createBitmap(width, height).apply {
        setPixels(pixels, 0, width, 0, 0, width, height)
    }
} catch (e: Exception) {
    null
}

