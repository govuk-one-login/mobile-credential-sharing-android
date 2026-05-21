package uk.gov.onelogin.sharing.verification.models

import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import org.junit.Test
import uk.gov.onelogin.sharing.verification.ClassInfoExt.assertInterfaceReturnTypes
import uk.gov.onelogin.sharing.verification.ClassInfoExt.scanResult

@OptIn(ExperimentalTime::class)
class CertificateValidityPeriodTest {
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
}
