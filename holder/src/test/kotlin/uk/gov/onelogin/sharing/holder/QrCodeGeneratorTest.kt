package uk.gov.onelogin.sharing.holder

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import uk.gov.onelogin.sharing.verifier.QrCodeGenerator.INVALID_QR_CODE_DATA
import uk.gov.onelogin.sharing.verifier.QrCodeGenerator.QR_CODE_DATA
import uk.gov.onelogin.sharing.verifier.QrCodeGenerator.QR_CODE_SIZE

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
}
