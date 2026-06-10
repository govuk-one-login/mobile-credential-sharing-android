package uk.gov.onelogin.sharing.verification.format.document.validity

import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

@OptIn(ExperimentalTime::class)
class IssuerAuthResultTest {

    private val now = Clock.System.now()

    private val result = IssuerAuthResult(
        certificateValidityPeriod = CertificateValidityPeriod(
            notBefore = now.minus(1.minutes),
            notAfter = now.plus(1.minutes)
        ),
        msoPayload = byteArrayOf(0x01, 0x02, 0x03),
        subjectCountry = "GB",
        subjectState = "London"
    )

    @Test
    fun `equals returns true for same content`() {
        val other = result.copy(msoPayload = byteArrayOf(0x01, 0x02, 0x03))
        assertEquals(result, other)
    }

    @Test
    fun `equals returns false for different msoPayload`() {
        val other = result.copy(msoPayload = byteArrayOf(0x04, 0x05))
        assertNotEquals(result, other)
    }

    @Test
    fun `equals returns false for different subjectCountry`() {
        val other = result.copy(subjectCountry = "US")
        assertNotEquals(result, other)
    }

    @Test
    fun `equals returns false for different subjectState`() {
        val other = result.copy(subjectState = "Manchester")
        assertNotEquals(result, other)
    }

    @Test
    fun `equals returns false for null subjectState vs non-null`() {
        val other = result.copy(subjectState = null)
        assertNotEquals(result, other)
    }

    @Test
    fun `hashCode is consistent for equal objects`() {
        val other = result.copy(msoPayload = byteArrayOf(0x01, 0x02, 0x03))
        assertEquals(result.hashCode(), other.hashCode())
    }

    @Test
    fun `hashCode differs for different msoPayload`() {
        val other = result.copy(msoPayload = byteArrayOf(0x04, 0x05))
        assertNotEquals(result.hashCode(), other.hashCode())
    }
}
