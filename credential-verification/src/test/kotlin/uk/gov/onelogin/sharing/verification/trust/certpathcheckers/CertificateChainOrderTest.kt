package uk.gov.onelogin.sharing.verification.trust.certpathcheckers

import io.mockk.every
import io.mockk.mockk
import java.security.cert.X509Certificate
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import uk.gov.onelogin.sharing.verification.trust.orderCertificates

class CertificateChainOrderTest {

    @Test
    fun `single cert returns it unchanged`() {
        val cert = mockk<X509Certificate>(relaxed = true)
        val result = orderCertificates(listOf(cert))
        assertThat(result.size, equalTo(1))
        assertThat(result[0], equalTo(cert))
    }

    @Test
    fun `orders leaf-intermediate-root from shuffled input`() {
        val root = certWithSkiAndAki(ski = "aa", aki = null)
        val intermediate = certWithSkiAndAki(ski = "bb", aki = "aa")
        val leaf = certWithSkiAndAki(ski = "cc", aki = "bb")

        val result = orderCertificates(listOf(root, leaf, intermediate))

        assertThat(result, equalTo(listOf(leaf, intermediate, root)))
    }

    @Test
    fun `preserves already-ordered chain`() {
        val root = certWithSkiAndAki(ski = "aa", aki = null)
        val leaf = certWithSkiAndAki(ski = "bb", aki = "aa")

        val result = orderCertificates(listOf(leaf, root))

        assertThat(result, equalTo(listOf(leaf, root)))
    }

    @Test
    fun `with no SKI or AKI returns original order`() {
        val cert1 = certWithSkiAndAki(ski = null, aki = null)
        val cert2 = certWithSkiAndAki(ski = null, aki = null)

        val result = orderCertificates(listOf(cert1, cert2))

        assertThat(result.first(), equalTo(cert1))
    }

    private fun certWithSkiAndAki(ski: String?, aki: String?): X509Certificate {
        val cert = mockk<X509Certificate>(relaxed = true)
        every { cert.getExtensionValue("2.5.29.14") } returns ski?.let { encodeSki(it) }
        every { cert.getExtensionValue("2.5.29.35") } returns aki?.let { encodeAki(it) }
        return cert
    }

    private fun encodeSki(hex: String): ByteArray {
        val keyId = hex.hexToByteArray()
        val inner = byteArrayOf(0x04, keyId.size.toByte()) + keyId
        return byteArrayOf(0x04, inner.size.toByte()) + inner
    }

    private fun encodeAki(hex: String): ByteArray {
        val keyId = hex.hexToByteArray()
        val kidTlv = byteArrayOf(0x80.toByte(), keyId.size.toByte()) + keyId
        val seq = byteArrayOf(0x30, kidTlv.size.toByte()) + kidTlv
        return byteArrayOf(0x04, seq.size.toByte()) + seq
    }

    private fun String.hexToByteArray(): ByteArray =
        chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}
