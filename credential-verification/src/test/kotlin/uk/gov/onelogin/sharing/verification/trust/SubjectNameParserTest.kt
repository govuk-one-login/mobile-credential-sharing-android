package uk.gov.onelogin.sharing.verification.trust

import io.mockk.every
import io.mockk.mockk
import java.security.cert.X509Certificate
import javax.security.auth.x500.X500Principal
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class SubjectNameParserTest {

    @Test
    fun `extracts country from subject`() {
        val cert = certWithSubject("C=GB,CN=Test")

        val result = parseSubjectName(cert)

        assertThat(result[OID_COUNTRY], equalTo("GB"))
    }

    @Test
    fun `extracts state from subject`() {
        val cert = certWithSubject("ST=England,C=GB,CN=Test")

        val result = parseSubjectName(cert)

        assertThat(result[OID_STATE_OR_PROVINCE], equalTo("England"))
    }

    @Test
    fun `returns empty map when no matching attributes`() {
        val cert = certWithSubject("CN=Test,O=Example")

        val result = parseSubjectName(cert)

        assertThat(result.isEmpty(), equalTo(true))
    }

    @Test
    fun `extracts both country and state`() {
        val cert = certWithSubject("C=GB,ST=England,CN=Test")

        val result = parseSubjectName(cert)

        assertThat(result[OID_COUNTRY], equalTo("GB"))
        assertThat(result[OID_STATE_OR_PROVINCE], equalTo("England"))
    }

    private fun certWithSubject(dn: String): X509Certificate {
        val cert = mockk<X509Certificate>(relaxed = true)
        val principal = mockk<X500Principal>()
        every { cert.subjectX500Principal } returns principal
        every { principal.getName("RFC2253") } returns dn
        return cert
    }
}
