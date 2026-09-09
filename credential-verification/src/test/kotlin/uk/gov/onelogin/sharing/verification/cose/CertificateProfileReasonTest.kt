package uk.gov.onelogin.sharing.verification.cose

import io.github.classgraph.FieldInfo
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import uk.gov.onelogin.sharing.verification.ClassInfoExt.scanResult

internal class CertificateProfileReasonTest {

    @Test
    fun `CertificateProfileReason has all expected enum values`() {
        val expectedValues = setOf(
            "INVALID_VALIDITY_PERIOD",
            "INVALID_KEY_USAGE",
            "INVALID_EXTENDED_KEY_USAGE",
            "MISSING_CRITICAL_EXTENSION",
            "UNSUPPORTED_ALGORITHM",
            "CROSS_PURPOSE_REJECTION"
        )

        val classInfo = scanResult.getClassInfo(CertificateProfileReason::class.java.name)
        val enumValues = classInfo.enumConstants.map(FieldInfo::getName).toSet()

        assertThat(enumValues, equalTo(expectedValues))
    }
}
