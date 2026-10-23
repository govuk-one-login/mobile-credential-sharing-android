package uk.gov.onelogin.sharing.holder

import android.graphics.Bitmap
import android.util.Log
import androidx.core.graphics.createBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

/**
 * Generates a QR code bitmap from the given string data.
 *
 * @param data The string to be encoded into the QR code.
 * @param size The desired width and height of the QR code in pixels.
 * @return A [Bitmap] object representing the QR code, or `null` if an error occurs during
 * generation.
 */

private const val TAG = "QRCodeGenerator"
private const val QR_CODE_BLACK = 0xFF000000.toInt()
private const val QR_CODE_WHITE = 0xFFFFFFFF.toInt()
private const val DEFAULT_QR_MARGIN = 4

fun qrCodeGenerator(data: String, size: Int, margin: Int = DEFAULT_QR_MARGIN): Bitmap? = try {
    val writer = QRCodeWriter()
    val hints = mapOf(EncodeHintType.MARGIN to margin)
    val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, size, size, hints)
    val width = bitMatrix.width
    val height = bitMatrix.height
    val pixels = IntArray(width * height) { i ->
        if (bitMatrix.get(i % width, i / width)) QR_CODE_BLACK else QR_CODE_WHITE
    }
    createBitmap(width, height).apply {
        setPixels(pixels, 0, width, 0, 0, width, height)
    }
} catch (e: IllegalArgumentException) {
    Log.e(TAG, "Error generating QR code", e)
    null
}
