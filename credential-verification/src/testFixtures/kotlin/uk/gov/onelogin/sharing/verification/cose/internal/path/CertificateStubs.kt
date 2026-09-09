package uk.gov.onelogin.sharing.verification.cose.internal.path

import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.cert.X509Certificate
import java.security.spec.ECGenParameterSpec

/**
 * Pre-generated test certificate stubs for use across trust verification tests.
 *
 * Provides key pairs and commonly-used certificate configurations so tests can
 * reference them directly.
 */
object CertificateStubs {
    // Key pairs
    val rootKeyPair: KeyPair = generateKeyPair()
    val intermediateKeyPair: KeyPair = generateKeyPair()
    val leafKeyPair: KeyPair = generateKeyPair()
    val inter2KeyPair: KeyPair = generateKeyPair()
    val untrustedRootKeyPair: KeyPair = generateKeyPair()
    val wrongKeyPair: KeyPair = generateKeyPair()

    private const val ROOT_DN = "CN=Root,C=GB,ST=London"
    private const val INTERMEDIATE_DN = "CN=Intermediate,C=GB,ST=London"
    private const val LEAF_DN = "CN=Leaf,C=GB,ST=London"

    // Valid certificates
    val rootCa: X509Certificate = TestCertificateGenerator(
        subject = ROOT_DN,
        keyPair = rootKeyPair,
        issuerKeyPair = rootKeyPair,
        issuer = ROOT_DN
    ).ca().build()

    val intermediateCa: X509Certificate = TestCertificateGenerator(
        subject = INTERMEDIATE_DN,
        keyPair = intermediateKeyPair,
        issuerKeyPair = rootKeyPair,
        issuer = ROOT_DN
    ).ca().build()

    val leaf: X509Certificate = TestCertificateGenerator(
        subject = LEAF_DN,
        keyPair = leafKeyPair,
        issuerKeyPair = intermediateKeyPair,
        issuer = INTERMEDIATE_DN
    ).leaf().build()

    val leafSignedByRoot: X509Certificate = TestCertificateGenerator(
        subject = LEAF_DN,
        keyPair = leafKeyPair,
        issuerKeyPair = rootKeyPair,
        issuer = ROOT_DN
    ).leaf().build()

    // Invalid: validity period
    val expiredLeaf: X509Certificate = TestCertificateGenerator(
        subject = LEAF_DN,
        keyPair = leafKeyPair,
        issuerKeyPair = rootKeyPair,
        issuer = ROOT_DN
    ).leaf().expired().build()

    val notYetValidLeaf: X509Certificate = TestCertificateGenerator(
        subject = LEAF_DN,
        keyPair = leafKeyPair,
        issuerKeyPair = rootKeyPair,
        issuer = ROOT_DN
    ).leaf().notYetValid().build()

    // Invalid: BasicConstraints
    val caWithoutBasicConstraints: X509Certificate = TestCertificateGenerator(
        subject = INTERMEDIATE_DN,
        keyPair = intermediateKeyPair,
        issuerKeyPair = rootKeyPair,
        issuer = ROOT_DN
    ).caWithoutBasicConstraints().build()

    val caWithCaFlagFalse: X509Certificate = TestCertificateGenerator(
        subject = INTERMEDIATE_DN,
        keyPair = intermediateKeyPair,
        issuerKeyPair = rootKeyPair,
        issuer = ROOT_DN
    ).caWithCaFlagFalse().build()

    val intermediateAsLeaf: X509Certificate = TestCertificateGenerator(
        subject = INTERMEDIATE_DN,
        keyPair = intermediateKeyPair,
        issuerKeyPair = rootKeyPair,
        issuer = ROOT_DN
    ).leaf().build()

    val caNotCriticalBasicConstraints: X509Certificate = TestCertificateGenerator(
        subject = INTERMEDIATE_DN,
        keyPair = intermediateKeyPair,
        issuerKeyPair = rootKeyPair,
        issuer = ROOT_DN
    ).caNotCriticalBasicConstraints().build()

    val leafWithBasicConstraints: X509Certificate = TestCertificateGenerator(
        subject = LEAF_DN,
        keyPair = leafKeyPair,
        issuerKeyPair = rootKeyPair,
        issuer = ROOT_DN
    ).leafWithBasicConstraints().build()

    // Invalid: KeyUsage
    val caKeyCertSignOnly: X509Certificate = TestCertificateGenerator(
        subject = INTERMEDIATE_DN,
        keyPair = intermediateKeyPair,
        issuerKeyPair = intermediateKeyPair,
        issuer = INTERMEDIATE_DN
    ).caKeyCertSignOnly().build()

    val caWithExtraKeyUsageBits: X509Certificate = TestCertificateGenerator(
        subject = INTERMEDIATE_DN,
        keyPair = intermediateKeyPair,
        issuerKeyPair = intermediateKeyPair,
        issuer = INTERMEDIATE_DN
    ).caWithExtraKeyUsageBits().build()

    val caWithLeafKeyUsage: X509Certificate = TestCertificateGenerator(
        subject = INTERMEDIATE_DN,
        keyPair = intermediateKeyPair,
        issuerKeyPair = intermediateKeyPair,
        issuer = INTERMEDIATE_DN
    ).caWithLeafKeyUsage().build()

    val caWithoutKeyUsage: X509Certificate = TestCertificateGenerator(
        subject = INTERMEDIATE_DN,
        keyPair = intermediateKeyPair,
        issuerKeyPair = rootKeyPair,
        issuer = ROOT_DN
    ).caWithoutKeyUsage().build()

    val leafNoKeyUsage: X509Certificate = TestCertificateGenerator(
        subject = LEAF_DN,
        keyPair = leafKeyPair,
        issuerKeyPair = rootKeyPair,
        issuer = ROOT_DN
    ).noKeyUsage().build()

    val leafWithCaKeyUsage: X509Certificate = TestCertificateGenerator(
        subject = LEAF_DN,
        keyPair = leafKeyPair,
        issuerKeyPair = rootKeyPair,
        issuer = ROOT_DN
    ).caKeyUsage().build()

    val leafWithNonCriticalKeyUsage: X509Certificate = TestCertificateGenerator(
        subject = LEAF_DN,
        keyPair = leafKeyPair,
        issuerKeyPair = rootKeyPair,
        issuer = ROOT_DN
    ).leafWithNonCriticalKeyUsage().build()

    val leafWithExtraBits: X509Certificate = TestCertificateGenerator(
        subject = LEAF_DN,
        keyPair = leafKeyPair,
        issuerKeyPair = rootKeyPair,
        issuer = ROOT_DN
    ).leafWithExtraBits().build()

    // Invalid: AKI mismatch
    val leafWithWrongAki: X509Certificate = TestCertificateGenerator(
        subject = LEAF_DN,
        keyPair = leafKeyPair,
        issuerKeyPair = rootKeyPair,
        issuer = ROOT_DN
    ).leaf().withAki(wrongKeyPair).build()

    // Invalid: untrusted chain
    val untrustedRoot: X509Certificate = TestCertificateGenerator(
        subject = "CN=Untrusted,C=GB,ST=London",
        keyPair = untrustedRootKeyPair,
        issuerKeyPair = untrustedRootKeyPair,
        issuer = "CN=Untrusted,C=GB,ST=London"
    ).ca().build()

    val leafSignedByUntrusted: X509Certificate = TestCertificateGenerator(
        subject = LEAF_DN,
        keyPair = leafKeyPair,
        issuerKeyPair = untrustedRootKeyPair,
        issuer = "CN=Untrusted,C=GB,ST=London"
    ).leaf().build()

    // Invalid: intermediate signed by wrong key
    val intermediateSignedByWrongKey: X509Certificate = TestCertificateGenerator(
        subject = INTERMEDIATE_DN,
        keyPair = intermediateKeyPair,
        issuerKeyPair = wrongKeyPair,
        issuer = ROOT_DN
    ).ca().build()

    // PathLen constraint
    val intermediateWithPathLen0: X509Certificate = TestCertificateGenerator(
        subject = "CN=Inter1,C=GB,ST=London",
        keyPair = intermediateKeyPair,
        issuerKeyPair = rootKeyPair,
        issuer = ROOT_DN
    ).ca(pathLen = 0).build()

    val inter2Ca: X509Certificate = TestCertificateGenerator(
        subject = "CN=Inter2,C=GB,ST=London",
        keyPair = inter2KeyPair,
        issuerKeyPair = intermediateKeyPair,
        issuer = "CN=Inter1,C=GB,ST=London"
    ).ca().build()

    val leafSignedByInter2: X509Certificate = TestCertificateGenerator(
        subject = LEAF_DN,
        keyPair = leafKeyPair,
        issuerKeyPair = inter2KeyPair,
        issuer = "CN=Inter2,C=GB,ST=London"
    ).leaf().build()

    private fun generateKeyPair(): KeyPair {
        val gen = KeyPairGenerator.getInstance("EC")
        gen.initialize(ECGenParameterSpec("secp256r1"))
        return gen.generateKeyPair()
    }
}
