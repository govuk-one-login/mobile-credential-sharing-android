package uk.gov.onelogin.sharing.verification.crl

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import uk.gov.onelogin.sharing.verification.crl.CrlFixtures.DAY
import uk.gov.onelogin.sharing.verification.crl.CrlFixtures.NOW

/**
 * Integration test for [CrlDecoder] using real BouncyCastle-generated DER CRL fixtures.
 *
 * Tests that the JDK `CertificateFactory` parsing path in [CrlDecoder] correctly reads dates from
 * actual DER bytes. Date-boundary logic is covered without real bytes in [CrlCacheInspectorTest].
 */
class CrlDecoderIntegrationTest {

    @Test
    fun `reads thisUpdate and nextUpdate from a real DER CRL`() {
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
    fun `returns null for non-CRL bytes`() {
        assertNull(CrlDecoder.readValidity("not a crl".toByteArray()))
    }

    @Test
    fun `returns null for empty byte array`() {
        assertNull(CrlDecoder.readValidity(ByteArray(0)))
    }
}
