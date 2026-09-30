package uk.gov.onelogin.sharing.verification.cose.internal

import java.io.File
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import uk.gov.onelogin.sharing.verification.cose.CoseVerificationFailure.UntrustedCertificate
import uk.gov.onelogin.sharing.verification.cose.internal.path.CertificateChainValidatorImpl
import uk.gov.onelogin.sharing.verification.cose.internal.profile.CertificateProfileValidator
import uk.gov.onelogin.sharing.verification.cose.internal.profile.CertificatePurpose
import uk.gov.onelogin.sharing.verification.reader.SiaExtensionParser

class DvsP256Test {

    private val certFactory = CertificateFactory.getInstance("X.509")

    private fun loadCerts(filename: String): List<X509Certificate> {
        val file = File("/Users/shababmahmood/AndroidStudioProjects/mobile-credential-sharing-android/app/src/main/assets/$filename")
        return file.inputStream().use { stream ->
            certFactory.generateCertificates(stream).filterIsInstance<X509Certificate>()
        }
    }

    @Test
    fun `Test DVS_P256 chain positive validation`() {
        val allCerts = loadCerts("reader_dvs_p256_chain.der")
        val root = allCerts.first { it.subjectX500Principal == it.issuerX500Principal }
        val chain = allCerts.filter { it != root }

        val validator = CertificateChainValidatorImpl()
        validator.verify(chain, root)

        val profileValidator = CertificateProfileValidator()
        profileValidator.validate(chain, CertificatePurpose.READER_AUTH)

        val leaf = chain.first()
        val parser = SiaExtensionParser()
        val url = parser.extractPrivacyPolicyUrl(leaf)
        assertNotNull(url)
        assertEquals("https://example.gov.uk/privacy", url)
    }

    @Test
    fun `Test DVS_P384 chain positive validation`() {
        val allCerts = loadCerts("reader_dvs_p384_chain.der")
        val root = allCerts.first { it.subjectX500Principal == it.issuerX500Principal }
        val chain = allCerts.filter { it != root }

        val validator = CertificateChainValidatorImpl()
        validator.verify(chain, root)

        val profileValidator = CertificateProfileValidator()
        profileValidator.validate(chain, CertificatePurpose.READER_AUTH)

        val leaf = chain.first()
        val parser = SiaExtensionParser()
        val url = parser.extractPrivacyPolicyUrl(leaf)
        assertNotNull(url)
        assertEquals("https://example.gov.uk/privacy", url)
    }

    @Test
    fun `verifying P-256 chain against P-384 root throws UntrustedCertificate`() {
        val p256Certs = loadCerts("reader_dvs_p256_chain.der")
        val p256Root = p256Certs.first { it.subjectX500Principal == it.issuerX500Principal }
        val p256Chain = p256Certs.filter { it != p256Root }

        val p384Certs = loadCerts("reader_dvs_p384_chain.der")
        val p384Root = p384Certs.first { it.subjectX500Principal == it.issuerX500Principal }

        val validator = CertificateChainValidatorImpl()
        assertFailsWith<UntrustedCertificate> {
            validator.verify(p256Chain, p384Root)
        }
    }

    @Test
    fun `verifying P-384 chain against P-256 root throws UntrustedCertificate`() {
        val p384Certs = loadCerts("reader_dvs_p384_chain.der")
        val p384Root = p384Certs.first { it.subjectX500Principal == it.issuerX500Principal }
        val p384Chain = p384Certs.filter { it != p384Root }

        val p256Certs = loadCerts("reader_dvs_p256_chain.der")
        val p256Root = p256Certs.first { it.subjectX500Principal == it.issuerX500Principal }

        val validator = CertificateChainValidatorImpl()
        assertFailsWith<UntrustedCertificate> {
            validator.verify(p384Chain, p256Root)
        }
    }

    @Test
    fun `verifying P-256 chain against mock sharing test app root throws UntrustedCertificate`() {
        val p256Certs = loadCerts("reader_dvs_p256_chain.der")
        val p256Root = p256Certs.first { it.subjectX500Principal == it.issuerX500Principal }
        val p256Chain = p256Certs.filter { it != p256Root }

        val mockRoot = loadCerts("test_reader_auth_x509_certificate.der").first()

        val validator = CertificateChainValidatorImpl()
        assertFailsWith<UntrustedCertificate> {
            validator.verify(p256Chain, mockRoot)
        }
    }
}
