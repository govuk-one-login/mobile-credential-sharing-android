package uk.gov.onelogin.sharing.verification.cose

import io.github.classgraph.ClassInfo
import io.mockk.mockk
import java.security.cert.X509Certificate
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import uk.gov.onelogin.sharing.verification.ClassInfoExt.scanResult

internal class CoseVerificationResultTest {

    @Test
    fun `CoseVerificationResult has expected inheritors`() {
        val expectedInheritors = setOf(
            "Attached",
            "Detached",
            "KeyBased"
        )

        val classInfo = scanResult.getSubclasses(CoseVerificationResult::class.java)

        assertThat(
            classInfo.map(ClassInfo::getSimpleName).toSet(),
            equalTo<Set<String>>(expectedInheritors)
        )
    }

    @Test
    fun `Attached result implements equality correctly for byte arrays`() {
        val cert: X509Certificate = mockk()
        val payload1 = byteArrayOf(0x01)
        val payload2 = byteArrayOf(0x01)

        val res1 = CoseVerificationResult.Attached(cert, payload1)
        val res2 = CoseVerificationResult.Attached(cert, payload2)

        assertThat(res1, equalTo(res2))
        assertThat(res1.hashCode(), equalTo(res2.hashCode()))
    }
}
