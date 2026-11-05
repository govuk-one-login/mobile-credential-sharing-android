package uk.gov.onelogin.sharing.holder

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import uk.gov.onelogin.sharing.holder.QrCodeGenerator.INVALID_QR_CODE_DATA
import uk.gov.onelogin.sharing.holder.QrCodeGenerator.QR_CODE_DATA
import uk.gov.onelogin.sharing.holder.QrCodeGenerator.QR_CODE_SIZE

@RunWith(RobolectricTestRunner::class)
class QrCodeGeneratorTest {

    @Test
    fun `returns a valid bitmap`() {
        val bitmap = qrCodeGenerator(data = QR_CODE_DATA, size = QR_CODE_SIZE)
        assertNotNull(bitmap)
    }

    @Test
    fun `returns null when data is empty`() {
        val bitmap = qrCodeGenerator(data = INVALID_QR_CODE_DATA, size = QR_CODE_SIZE)
        assertNull(bitmap)
    }

    @Test
    fun `different data produces visually different bitmaps`() {
        val first = qrCodeGenerator(data = "mdoc:ENGAGEMENT_AAA", size = QR_CODE_SIZE)
        val second = qrCodeGenerator(data = "mdoc:ENGAGEMENT_BBB", size = QR_CODE_SIZE)

        assertNotNull(first)
        assertNotNull(second)

        val firstPixels = IntArray(first!!.width * first.height)
        first.getPixels(firstPixels, 0, first.width, 0, 0, first.width, first.height)
        val secondPixels = IntArray(second!!.width * second.height)
        second.getPixels(secondPixels, 0, second.width, 0, 0, second.width, second.height)

        org.junit.Assert.assertFalse(
            "Different engagement strings must yield different QR pixels",
            firstPixels.contentEquals(secondPixels)
        )
    }
}
