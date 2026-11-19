package uk.gov.onelogin.sharing.verification.crl

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import uk.gov.onelogin.sharing.verification.crl.CrlFixtures.DAY
import uk.gov.onelogin.sharing.verification.crl.CrlFixtures.NOW

class CrlDecoderTest {
    @Test
    fun `reads this update and next update`() {
        val bytes = CrlFixtures.crl(thisUpdate = NOW - DAY, nextUpdate = NOW + DAY)

        assertEquals(CrlValidity(NOW - DAY, NOW + DAY), CrlDecoder.readValidity(bytes))
    }

    @Test
    fun `crl without next update is unreadable`() {
        assertNull(CrlDecoder.readValidity(CrlFixtures.crl(NOW - DAY, nextUpdate = null)))
    }

    @Test
    fun `non crl bytes are unreadable`() {
        assertNull(CrlDecoder.readValidity("not a crl".toByteArray()))
        assertNull(CrlDecoder.readValidity(ByteArray(0)))
    }
}
