package uk.gov.onelogin.sharing.verification.cose

import io.github.classgraph.ClassInfo
import io.mockk.mockk
import java.security.cert.X509Certificate
import java.security.interfaces.ECPublicKey
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import uk.gov.onelogin.sharing.verification.ClassInfoExt.scanResult

internal class CoseVerificationRequestTest {

    @Test
    fun `CoseVerificationRequest has expected inheritors`() {
        val expectedInheritors = setOf(
            "Attached",
            "Detached",
            "KeyBased"
        )

        val classInfo = scanResult.getSubclasses(CoseVerificationRequest::class.java)

        assertThat(
            classInfo.map(ClassInfo::getSimpleName).toSet(),
            equalTo<Set<String>>(expectedInheritors)
        )
    }

    @Test
    fun `Attached variant implements equality correctly for byte arrays`() {
        val root: X509Certificate = mockk()
        val bytes1 = byteArrayOf(0x01, 0x02)
        val bytes2 = byteArrayOf(0x01, 0x02)
        val bytes3 = byteArrayOf(0x03, 0x04)

        val req1 = CoseVerificationRequest.Attached(bytes1, root)
        val req2 = CoseVerificationRequest.Attached(bytes2, root)
        val req3 = CoseVerificationRequest.Attached(bytes3, root)

        assertThat(req1, equalTo(req2))
        assertThat(req1.hashCode(), equalTo(req2.hashCode()))
        assertThat(req1, not(equalTo(req3)))
    }

    @Test
    fun `Detached variant implements equality correctly for byte arrays`() {
        val root: X509Certificate = mockk()
        val coseBytes = byteArrayOf(0x01)
        val payload1 = byteArrayOf(0x02)
        val payload2 = byteArrayOf(0x02)

        val req1 = CoseVerificationRequest.Detached(coseBytes, payload1, root)
        val req2 = CoseVerificationRequest.Detached(coseBytes, payload2, root)

        assertThat(req1, equalTo(req2))
        assertThat(req1.hashCode(), equalTo(req2.hashCode()))
    }

    @Test
    fun `KeyBased variant implements equality correctly for byte arrays`() {
        val key: ECPublicKey = mockk()
        val coseBytes = byteArrayOf(0x01)
        val payload = byteArrayOf(0x02)

        val req1 = CoseVerificationRequest.KeyBased(coseBytes, payload, key)
        val req2 = CoseVerificationRequest.KeyBased(coseBytes, payload, key)

        assertThat(req1, equalTo(req2))
        assertThat(req1.hashCode(), equalTo(req2.hashCode()))
    }
}
