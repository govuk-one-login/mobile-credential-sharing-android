package uk.gov.onelogin.sharing.verification.models

import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Test
import uk.gov.onelogin.sharing.verification.ClassInfoExt.assertInterfaceReturnTypes
import uk.gov.onelogin.sharing.verification.ClassInfoExt.scanResult

@OptIn(ExperimentalTime::class)
class CertificateValidityPeriodTest {

    private val now = Clock.System.now()

    private val period = CertificateValidityPeriod(
        notBefore = now.minus(1.minutes),
        notAfter = now.plus(1.minutes)
    )

    private val differentBefore = period.copy(
        notBefore = period.notBefore.minus(1.minutes)
    )

    private val differentAfter = period.copy(
        notAfter = period.notAfter.plus(1.minutes)
    )

    /**
     * DCMAW-20245: AC9: [CertificateValidityPeriod] carries `notBefore` and `notAfter` as
     * timezone-aware timestamps.
     */
    @Test
    fun `Ensure VerifiableDocument constraints`() {
        val expectedMethods = listOf(
            "getNotBefore" to Instant::class.java,
            "getNotAfter" to Instant::class.java
        )

        val classInfo = scanResult.getClassInfo(CertificateValidityPeriod::class.java.name)

        assertInterfaceReturnTypes(expectedMethods, classInfo)
    }

    @Test
    fun `Equality contract`() {
        assertEquals(period, period.copy())

        assertFalse(period.equals("different type"))
        assertNotEquals(period, differentBefore)
        assertNotEquals(period, differentAfter)
    }

    @Test
    fun `Hashcode contract`() {
        assertEquals(period.hashCode(), period.copy().hashCode())

        assertNotEquals(period.hashCode(), differentBefore.hashCode())
        assertNotEquals(period.hashCode(), differentAfter.hashCode())
    }
}
