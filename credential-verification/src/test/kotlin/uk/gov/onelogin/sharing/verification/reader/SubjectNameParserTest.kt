package uk.gov.onelogin.sharing.verification.reader

import java.security.KeyPairGenerator
import java.security.spec.ECGenParameterSpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import uk.gov.onelogin.sharing.verification.cose.internal.path.CertificateStubs
import uk.gov.onelogin.sharing.verification.cose.internal.path.TestCertificateGenerator

class SubjectNameParserTest {

    private val parser = SubjectNameParser()
    private val keyPair = generateKeyPair()

    @Test
    fun `extracts Organization name when present`() {
        val expectedOrg = "GOV.UK OneLogin Reader"
        val cert = TestCertificateGenerator(
            subject = "CN=Reader Leaf,O=$expectedOrg,C=GB",
            keyPair = keyPair,
            issuerKeyPair = CertificateStubs.rootKeyPair,
            issuer = "CN=Root CA"
        ).leaf().build()

        val orgName = parser.extractOrganizationName(cert)

        assertEquals(expectedOrg, orgName)
    }

    @Test
    fun `extracts Organization name when CN contains an escaped comma`() {
        val expectedOrg = "GOV.UK OneLogin Reader"
        val cert = TestCertificateGenerator(
            subject = "CN=DVLA\\, Driver & Vehicle Licensing Agency,O=$expectedOrg,C=GB",
            keyPair = keyPair,
            issuerKeyPair = CertificateStubs.rootKeyPair,
            issuer = "CN=Root CA"
        ).leaf().build()

        val orgName = parser.extractOrganizationName(cert)

        assertEquals(expectedOrg, orgName)
    }

    @Test
    fun `extracts Organization name when O contains an escaped comma`() {
        val expectedOrg = "GOV.UK, OneLogin Reader"
        val cert = TestCertificateGenerator(
            subject = "CN=DVLA,O=GOV.UK\\, OneLogin Reader,C=GB",
            keyPair = keyPair,
            issuerKeyPair = CertificateStubs.rootKeyPair,
            issuer = "CN=Root CA"
        ).leaf().build()

        val orgName = parser.extractOrganizationName(cert)

        assertEquals(expectedOrg, orgName)
    }

    @Test
    fun `returns null when Organization name is missing from DN`() {
        val cert = TestCertificateGenerator(
            subject = "CN=Reader Leaf,C=GB",
            keyPair = keyPair,
            issuerKeyPair = CertificateStubs.rootKeyPair,
            issuer = "CN=Root CA"
        ).leaf().build()

        val orgName = parser.extractOrganizationName(cert)

        assertNull(orgName)
    }

    private fun generateKeyPair(): java.security.KeyPair {
        val gen = KeyPairGenerator.getInstance("EC")
        gen.initialize(ECGenParameterSpec("secp256r1"))
        return gen.generateKeyPair()
    }
}
