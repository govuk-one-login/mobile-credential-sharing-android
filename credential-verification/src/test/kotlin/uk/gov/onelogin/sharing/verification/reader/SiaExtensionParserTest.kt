package uk.gov.onelogin.sharing.verification.reader

import java.security.KeyPairGenerator
import java.security.spec.ECGenParameterSpec
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.x509.AccessDescription
import org.bouncycastle.asn1.x509.GeneralName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import uk.gov.onelogin.sharing.verification.cose.internal.path.CertificateStubs
import uk.gov.onelogin.sharing.verification.cose.internal.path.TestCertificateGenerator

class SiaExtensionParserTest {

    private lateinit var parser: SiaExtensionParser
    private val keyPair = generateKeyPair()

    @Before
    fun setUp() {
        parser = SiaExtensionParser()
    }

    @Test
    fun `extracts privacy policy URL from SIA extension`() {
        val expectedUrl = "https://example.gov.uk/privacy"
        val siaBytes = buildSiaExtensionValue(SIA_PRIVACY_OID, expectedUrl)

        val cert = TestCertificateGenerator(
            subject = "CN=Reader Leaf,O=Test Org,C=GB",
            keyPair = keyPair,
            issuerKeyPair = CertificateStubs.rootKeyPair,
            issuer = "CN=Root CA"
        ).leaf().withExtension(OID_SIA, false, siaBytes).build()

        val extractedUrl = parser.extractPrivacyPolicyUrl(cert)

        assertEquals(expectedUrl, extractedUrl)
    }

    @Test
    fun `returns null when certificate has no SIA extension`() {
        val cert = TestCertificateGenerator(
            subject = "CN=Reader Leaf,O=Test Org,C=GB",
            keyPair = keyPair,
            issuerKeyPair = CertificateStubs.rootKeyPair,
            issuer = "CN=Root CA"
        ).leaf().build()

        assertNull(parser.extractPrivacyPolicyUrl(cert))
    }

    @Test
    fun `returns null when SIA extension does not contain privacy policy OID`() {
        val wrongOidSia = buildSiaExtensionValue("1.3.6.1.5.5.7.48.1", "https://example.com/ocsp")

        val cert = TestCertificateGenerator(
            subject = "CN=Reader Leaf,O=Test Org,C=GB",
            keyPair = keyPair,
            issuerKeyPair = CertificateStubs.rootKeyPair,
            issuer = "CN=Root CA"
        ).leaf().withExtension(OID_SIA, false, wrongOidSia).build()

        assertNull(parser.extractPrivacyPolicyUrl(cert))
    }

    private fun buildSiaExtensionValue(accessMethodOid: String, uriString: String): ByteArray {
        val accessMethod = ASN1ObjectIdentifier(accessMethodOid)
        val location = GeneralName(GeneralName.uniformResourceIdentifier, uriString)
        val desc = AccessDescription(accessMethod, location)
        val siaSeq = DERSequence(desc)
        return siaSeq.encoded
    }

    private fun generateKeyPair(): java.security.KeyPair {
        val gen = KeyPairGenerator.getInstance("EC")
        gen.initialize(ECGenParameterSpec("secp256r1"))
        return gen.generateKeyPair()
    }

    private companion object {
        const val OID_SIA = "1.3.6.1.5.5.7.1.11"
        const val SIA_PRIVACY_OID = "1.3.6.1.4.1.66559.1.1"
    }
}
