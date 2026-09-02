package uk.gov.onelogin.sharing.verification.cose

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import uk.gov.onelogin.sharing.verification.ClassInfoExt.scanResult

internal class CoseVerifierTest {

    @Test
    fun `CoseVerifier exposes verify method with correct signature`() {
        val classInfo = scanResult.getClassInfo(CoseVerifier::class.java.name)
        val methodInfo = classInfo.methodInfo

        assertThat(methodInfo.size, equalTo(1))

        val verifyMethod = methodInfo.first()
        assertThat(verifyMethod.name, equalTo("verify"))

        val expectedDescriptor = "${CoseVerificationResult::class.java.name} " +
            "(${CoseVerificationRequest::class.java.name})"

        assertThat(
            verifyMethod.typeDescriptor.toString(),
            equalTo(expectedDescriptor)
        )
    }
}
