package uk.gov.onelogin.sharing.holder

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class QrCodeGeneratorTest {

    @Test
    fun returnABitmapWhenDataNotNull() {
        val bitmap = qrCodeGenerator(data = "www.test.com", size = 512)
        assertNotNull(bitmap)
    }

    @Test
    fun returnNullWhenDataIsNull() {
        val bitmap = qrCodeGenerator(data = "", size = 512)
        assertNull(bitmap)
    }
}