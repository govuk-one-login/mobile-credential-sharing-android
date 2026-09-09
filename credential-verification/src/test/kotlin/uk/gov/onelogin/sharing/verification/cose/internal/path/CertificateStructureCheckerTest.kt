package uk.gov.onelogin.sharing.verification.cose.internal.path

import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.spec.ECGenParameterSpec
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.verification.cose.CoseVerificationFailure

@RunWith(TestParameterInjector::class)
class CertificateStructureCheckerTest {
    private val validator = CertificateChainValidatorImpl()

    @Test
    fun `valid structural integrity passes`() {
        validator.verify(
            listOf(CertificateStubs.leafSignedByRoot),
            CertificateStubs.rootCa
        )
    }

    @Test
    fun `valid chain with intermediate passes`() {
        validator.verify(
            listOf(CertificateStubs.leaf, CertificateStubs.intermediateCa),
            CertificateStubs.rootCa
        )
    }

    // AC2: Disallowed critical extension
    @Test
    fun `cert with disallowed critical extension throws UNTRUSTED_CERTIFICATE`(
        @TestParameter(valuesProvider = DisallowedCriticalExtProvider::class) case: StructureCase
    ) {
        assertThrows(CoseVerificationFailure.UntrustedCertificate::class.java) {
            validator.verify(case.chain, case.root)
        }
    }

    // AC3: Forbidden extension present
    @Test
    fun `cert with forbidden extension throws UNTRUSTED_CERTIFICATE`(
        @TestParameter(valuesProvider = ForbiddenExtensionProvider::class) case: StructureCase
    ) {
        assertThrows(CoseVerificationFailure.UntrustedCertificate::class.java) {
            validator.verify(case.chain, case.root)
        }
    }

    // AC5: Invalid serial number
    @Test
    fun `cert with invalid serial number throws UNTRUSTED_CERTIFICATE`(
        @TestParameter(valuesProvider = InvalidSerialProvider::class) case: StructureCase
    ) {
        assertThrows(CoseVerificationFailure.UntrustedCertificate::class.java) {
            validator.verify(case.chain, case.root)
        }
    }

    // AC6: Invalid SubjectKeyIdentifier - absent
    @Test
    fun `cert without SKI throws UNTRUSTED_CERTIFICATE`() {
        val leaf = TestCertificateGenerator(
            subject = "CN=Leaf,C=GB,ST=London",
            keyPair = CertificateStubs.leafKeyPair,
            issuerKeyPair = CertificateStubs.rootKeyPair,
            issuer = "CN=Root,C=GB,ST=London"
        ).leaf().withoutSki().build()

        assertThrows(CoseVerificationFailure.UntrustedCertificate::class.java) {
            validator.verify(listOf(leaf), CertificateStubs.rootCa)
        }
    }

    // AC7: Invalid AuthorityKeyIdentifier - absent
    @Test
    fun `cert without AKI throws UNTRUSTED_CERTIFICATE`() {
        val leaf = TestCertificateGenerator(
            subject = "CN=Leaf,C=GB,ST=London",
            keyPair = CertificateStubs.leafKeyPair,
            issuerKeyPair = CertificateStubs.rootKeyPair,
            issuer = "CN=Root,C=GB,ST=London"
        ).leaf().withoutAki().build()

        assertThrows(CoseVerificationFailure.UntrustedCertificate::class.java) {
            validator.verify(listOf(leaf), CertificateStubs.rootCa)
        }
    }

    // AC8: Disallowed signing algorithm
    @Test
    fun `cert with RSA signing algorithm throws UNTRUSTED_CERTIFICATE`() {
        val rsaKeyPair = KeyPairGenerator.getInstance("RSA").apply {
            initialize(2048)
        }.generateKeyPair()

        val leaf = TestCertificateGenerator(
            subject = "CN=Leaf,C=GB,ST=London",
            keyPair = CertificateStubs.leafKeyPair,
            issuerKeyPair = rsaKeyPair,
            issuer = "CN=Root,C=GB,ST=London"
        ).leaf().withSignatureAlgorithm("SHA256withRSA").build()

        val root = TestCertificateGenerator(
            subject = "CN=Root,C=GB,ST=London",
            keyPair = rsaKeyPair,
            issuerKeyPair = rsaKeyPair,
            issuer = "CN=Root,C=GB,ST=London"
        ).ca().withSignatureAlgorithm("SHA256withRSA").build()

        assertThrows(CoseVerificationFailure.UntrustedCertificate::class.java) {
            validator.verify(listOf(leaf), root)
        }
    }

    // AC9: Insufficient algorithm strength
    @Test
    fun `cert signed with SHA256 under P-384 issuer throws UNTRUSTED_CERTIFICATE`() {
        val p384KeyPair = generateEcKeyPair("secp384r1")

        val root = TestCertificateGenerator(
            subject = "CN=Root,C=GB,ST=London",
            keyPair = p384KeyPair,
            issuerKeyPair = p384KeyPair,
            issuer = "CN=Root,C=GB,ST=London"
        ).ca().withSignatureAlgorithm("SHA384withECDSA").build()

        val leaf = TestCertificateGenerator(
            subject = "CN=Leaf,C=GB,ST=London",
            keyPair = CertificateStubs.leafKeyPair,
            issuerKeyPair = p384KeyPair,
            issuer = "CN=Root,C=GB,ST=London"
        ).leaf().withSignatureAlgorithm("SHA256withECDSA").build()

        assertThrows(CoseVerificationFailure.UntrustedCertificate::class.java) {
            validator.verify(listOf(leaf), root)
        }
    }

    @Test
    fun `cert signed with SHA384 under P-521 issuer throws UNTRUSTED_CERTIFICATE`() {
        val p521KeyPair = generateEcKeyPair("secp521r1")

        val root = TestCertificateGenerator(
            subject = "CN=Root,C=GB,ST=London",
            keyPair = p521KeyPair,
            issuerKeyPair = p521KeyPair,
            issuer = "CN=Root,C=GB,ST=London"
        ).ca().withSignatureAlgorithm("SHA512withECDSA").build()

        val leaf = TestCertificateGenerator(
            subject = "CN=Leaf,C=GB,ST=London",
            keyPair = CertificateStubs.leafKeyPair,
            issuerKeyPair = p521KeyPair,
            issuer = "CN=Root,C=GB,ST=London"
        ).leaf().withSignatureAlgorithm("SHA384withECDSA").build()

        assertThrows(CoseVerificationFailure.UntrustedCertificate::class.java) {
            validator.verify(listOf(leaf), root)
        }
    }

    // AC9: Valid strength
    @Test
    fun `cert signed with SHA384 under P-384 issuer passes`() {
        val p384KeyPair = generateEcKeyPair("secp384r1")

        val root = TestCertificateGenerator(
            subject = "CN=Root,C=GB,ST=London",
            keyPair = p384KeyPair,
            issuerKeyPair = p384KeyPair,
            issuer = "CN=Root,C=GB,ST=London"
        ).ca().withSignatureAlgorithm("SHA384withECDSA").build()

        val leaf = TestCertificateGenerator(
            subject = "CN=Leaf,C=GB,ST=London",
            keyPair = CertificateStubs.leafKeyPair,
            issuerKeyPair = p384KeyPair,
            issuer = "CN=Root,C=GB,ST=London"
        ).leaf().withSignatureAlgorithm("SHA384withECDSA").build()

        validator.verify(listOf(leaf), root)
    }

    @Test
    fun `cert signed with SHA512 under P-521 issuer passes`() {
        val p521KeyPair = generateEcKeyPair("secp521r1")

        val root = TestCertificateGenerator(
            subject = "CN=Root,C=GB,ST=London",
            keyPair = p521KeyPair,
            issuerKeyPair = p521KeyPair,
            issuer = "CN=Root,C=GB,ST=London"
        ).ca().withSignatureAlgorithm("SHA512withECDSA").build()

        val leaf = TestCertificateGenerator(
            subject = "CN=Leaf,C=GB,ST=London",
            keyPair = CertificateStubs.leafKeyPair,
            issuerKeyPair = p521KeyPair,
            issuer = "CN=Root,C=GB,ST=London"
        ).leaf().withSignatureAlgorithm("SHA512withECDSA").build()

        validator.verify(listOf(leaf), root)
    }

    private fun generateEcKeyPair(curve: String): KeyPair =
        KeyPairGenerator.getInstance("EC").apply {
            initialize(ECGenParameterSpec(curve))
        }.generateKeyPair()
}
