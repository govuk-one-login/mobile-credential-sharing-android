package uk.gov.onelogin.sharing.verification.reader

import android.net.Uri
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.namedTestValues
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import java.security.KeyPairGenerator
import java.security.cert.X509Certificate
import java.security.spec.ECGenParameterSpec
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.x509.AccessDescription
import org.bouncycastle.asn1.x509.GeneralName
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DocRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequest
import uk.gov.onelogin.sharing.verification.cose.internal.path.CertificateStubs
import uk.gov.onelogin.sharing.verification.cose.internal.path.TestCertificateGenerator

@RunWith(TestParameterInjector::class)
class ValidatePrivacyPolicyUseCaseImplTest {

    private lateinit var useCase: ValidatePrivacyPolicyUseCaseImpl

    private val sampleDocRequest = DocRequest(
        itemsRequest = ItemsRequest(docType = "org.iso.18013.5.1.mDL", nameSpaces = emptyMap())
    )

    private val keyPair = generateKeyPair()

    @Before
    fun setUp() {
        mockkStatic(Uri::class)
        every { Uri.parse(any()) } answers {
            val urlString = firstArg<String>()
            val mockUri = mockk<Uri>()
            every { mockUri.toString() } returns urlString
            every { mockUri.scheme } returns when {
                urlString.startsWith("https://", ignoreCase = true) -> "https"
                urlString.startsWith("http://", ignoreCase = true) -> "http"
                urlString.startsWith("ftp://", ignoreCase = true) -> "ftp"
                else -> null
            }
            every { mockUri.host } returns if (urlString.contains("://") &&
                !urlString.startsWith("https:///")
            ) {
                urlString.substringAfter("://")
                    .substringBefore('/')
                    .substringBefore('@')
            } else {
                null
            }
            every { mockUri.userInfo } returns if (urlString.contains("user:pass@")) {
                "user:pass"
            } else {
                null
            }
            every { mockUri.isAbsolute } returns urlString.contains("://")
            mockUri
        }
        useCase = ValidatePrivacyPolicyUseCaseImpl()
    }

    @After
    fun tearDown() {
        unmockkStatic(Uri::class)
    }

    @Test
    fun `valid SIA extension extracts privacy policy URL and organizationName`() {
        val validUrl = "https://example.gov.uk/privacy"
        val expectedOrg = "GOV.UK OneLogin Reader"
        val siaBytes = buildSiaExtensionValue(SIA_PRIVACY_OID, validUrl)

        val leafCert = TestCertificateGenerator(
            subject = "CN=Reader Leaf,O=$expectedOrg,C=GB",
            keyPair = keyPair,
            issuerKeyPair = CertificateStubs.rootKeyPair,
            issuer = "CN=Root CA"
        ).leaf().withExtension(OID_SIA, false, siaBytes).build()

        val verifiedRequest = VerifiedReaderRequest(sampleDocRequest, leafCert)
        val authenticatedRequest = useCase.validate(verifiedRequest)

        assertEquals(sampleDocRequest, authenticatedRequest.docRequest)
        assertEquals(validUrl, authenticatedRequest.privacyPolicyUrl.toString())
        assertEquals(expectedOrg, authenticatedRequest.readerOrganizationName)
    }

    @Test
    fun `missing SIA extension throws PRIVACY_POLICY_URL_INVALID`() {
        val leafCertWithoutSia = TestCertificateGenerator(
            subject = "CN=Reader Leaf,O=Test Org,C=GB",
            keyPair = keyPair,
            issuerKeyPair = CertificateStubs.rootKeyPair,
            issuer = "CN=Root CA"
        ).leaf().build()

        val failure = assertThrows(ReaderAuthenticationFailure::class.java) {
            useCase.validate(
                VerifiedReaderRequest(
                    sampleDocRequest,
                    leafCertWithoutSia
                )
            )
        }

        assertEquals(
            ReaderAuthenticationReason.PRIVACY_POLICY_URL_INVALID,
            failure.reason
        )
    }

    @Test
    fun `missing privacy policy OID in SIA throws PRIVACY_POLICY_URL_INVALID`() {
        val wrongOidSia = buildSiaExtensionValue(
            "1.3.6.1.5.5.7.48.1",
            "https://example.com/ocsp"
        )

        val leafCertWrongOid = TestCertificateGenerator(
            subject = "CN=Reader Leaf,O=Test Org,C=GB",
            keyPair = keyPair,
            issuerKeyPair = CertificateStubs.rootKeyPair,
            issuer = "CN=Root CA"
        ).leaf().withExtension(OID_SIA, false, wrongOidSia).build()

        val failure = assertThrows(ReaderAuthenticationFailure::class.java) {
            useCase.validate(VerifiedReaderRequest(sampleDocRequest, leafCertWrongOid))
        }

        assertEquals(ReaderAuthenticationReason.PRIVACY_POLICY_URL_INVALID, failure.reason)
    }

    @Test
    fun `invalid privacy policy URL violates conditions and throws PRIVACY_POLICY_URL_INVALID`(
        @TestParameter invalidUrl: String = namedTestValues(
            "HTTP scheme" to "http://example.gov.uk/privacy",
            "FTP scheme" to "ftp://example.gov.uk/privacy",
            "Missing scheme" to "example.gov.uk/privacy",
            "Empty host" to "https:///privacy",
            "Contains user info" to "https://user:pass@example.gov.uk/privacy",
            "Contains space" to "https://example.gov.uk/privacy policy",
            "Contains non-ASCII character" to "https://exämple.gov.uk/privacy",
            "Length > 2048 chars" to "https://example.gov.uk/" + "a".repeat(2040)
        )
    ) {
        val siaBytes = buildSiaExtensionValue(
            SIA_PRIVACY_OID,
            invalidUrl
        )

        val leafCert = TestCertificateGenerator(
            subject = "CN=Reader Leaf,O=Test Org,C=GB",
            keyPair = keyPair,
            issuerKeyPair = CertificateStubs.rootKeyPair,
            issuer = "CN=Root CA"
        ).leaf().withExtension(OID_SIA, false, siaBytes).build()

        val failure = assertThrows(ReaderAuthenticationFailure::class.java) {
            useCase.validate(VerifiedReaderRequest(sampleDocRequest, leafCert))
        }

        assertEquals(
            ReaderAuthenticationReason.PRIVACY_POLICY_URL_INVALID,
            failure.reason
        )
    }

    @Test
    fun `missing organizationName does not fail validation`() {
        val validUrl = "https://example.gov.uk/privacy"
        val siaBytes = buildSiaExtensionValue(SIA_PRIVACY_OID, validUrl)

        val leafCertNoOrg = TestCertificateGenerator(
            subject = "CN=Reader Leaf,C=GB",
            keyPair = keyPair,
            issuerKeyPair = CertificateStubs.rootKeyPair,
            issuer = "CN=Root CA"
        ).leaf().withExtension(OID_SIA, false, siaBytes).build()

        val authenticatedRequest = useCase.validate(
            VerifiedReaderRequest(
                sampleDocRequest,
                leafCertNoOrg
            )
        )

        assertEquals(validUrl, authenticatedRequest.privacyPolicyUrl.toString())
        assertEquals(null, authenticatedRequest.readerOrganizationName)
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
