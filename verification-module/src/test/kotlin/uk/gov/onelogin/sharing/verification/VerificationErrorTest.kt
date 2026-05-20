package uk.gov.onelogin.sharing.verification

import com.google.testing.junit.testparameterinjector.TestParameterInjector
import io.github.classgraph.FieldInfo
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.verification.ClassInfoExt.scanResult

@RunWith(TestParameterInjector::class)
class VerificationErrorTest {

    /**
     * DCMAW-20245: AC3: [VerificationError] contains exactly the 15 named values
     */
    @Test
    fun `There are 15 instances of VerificationError`() {
        val expectedPropertyNames = listOf(
            "DIGEST_MISMATCH",
            "DIGEST_MISSING",
            "INVALID_DEVICE_KEY",
            "INVALID_DEVICE_SIGNATURE",
            "INVALID_DOC_TYPE",
            "INVALID_ISSUER_SIGNATURE",
            "INVALID_MSO_VERSION",
            "MALFORMED_ISSUER_AUTH",
            "MALFORMED_MSO",
            "UNSUPPORTED_DIGEST_ALGORITHM",
            "UNTRUSTED_CERTIFICATE",
            "VALIDITY_FROM_OUT_OF_RANGE",
            "VALIDITY_SIGNED_OUT_OF_RANGE",
            "VALIDITY_UNTIL_EXPIRED",
            "VALIDITY_UNTIL_OUT_OF_RANGE",
        )

        val info = scanResult.getClassInfo(VerificationError::class.java.name)

        assertThat(
            info.enumConstants.map(FieldInfo::getName).toSet(),
            equalTo(expectedPropertyNames.toSet())
        )
    }
}