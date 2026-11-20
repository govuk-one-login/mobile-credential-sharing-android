package uk.gov.onelogin.sharing.verification.crl

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import uk.gov.onelogin.sharing.verification.crl.CrlFixtures.DAY
import uk.gov.onelogin.sharing.verification.crl.CrlFixtures.NOW

/**
 * Unit tests for [CrlDecoder]. Validates that [CrlDecoder.readValidity] correctly reads dates
 * from DER CRL bytes and returns null for unreadable or incomplete input.
 */
class CrlDecoderTest {

    @Test
    fun `reads thisUpdate and nextUpdate from a valid DER CRL`() {
        val bytes = CrlFixtures.crl(thisUpdate = NOW - DAY, nextUpdate = NOW + DAY)

        val validity = CrlDecoder.readValidity(bytes)

        assertEquals(CrlValidity(thisUpdate = NOW - DAY, nextUpdate = NOW + DAY), validity)
    }

    @Test
    fun `returns null when nextUpdate is absent`() {
        val bytes = CrlFixtures.crl(thisUpdate = NOW - DAY, nextUpdate = null)

        assertNull(CrlDecoder.readValidity(bytes))
    }

    @Test
    fun `returns null for a non-CRL byte sequence`() {
        assertNull(CrlDecoder.readValidity("not a crl".toByteArray()))
    }

    @Test
    fun `returns null for an empty byte array`() {
        assertNull(CrlDecoder.readValidity(ByteArray(0)))
    }
}
