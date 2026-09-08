package uk.gov.onelogin.sharing.verification.cose.internal.path

import com.google.testing.junit.testparameterinjector.TestParameterValuesProvider
import java.security.KeyPairGenerator
import java.security.cert.X509Certificate
import java.security.spec.ECGenParameterSpec

/**
 * Supplies [UnsupportedAlgorithmCase]s for `@TestParameter`.
 */
data class UnsupportedAlgorithmCase(
    val description: String,
    val chain: List<X509Certificate>,
    val root: X509Certificate
) {
    override fun toString(): String = description
}

class UnsupportedAlgorithmProvider : TestParameterValuesProvider() {
    override fun provideValues(context: Context?): List<UnsupportedAlgorithmCase> {
        val rsaKeyPair = KeyPairGenerator.getInstance("RSA").apply {
            initialize(2048)
        }.generateKeyPair()

        val rsaLeaf = TestCertificateGenerator(
            subject = "CN=Leaf,C=GB,ST=London",
            keyPair = CertificateStubs.leafKeyPair,
            issuerKeyPair = rsaKeyPair,
            issuer = "CN=Root,C=GB,ST=London"
        ).leaf().withSignatureAlgorithm("SHA256withRSA").build()

        val rsaRoot = TestCertificateGenerator(
            subject = "CN=Root,C=GB,ST=London",
            keyPair = rsaKeyPair,
            issuerKeyPair = rsaKeyPair,
            issuer = "CN=Root,C=GB,ST=London"
        ).ca().withSignatureAlgorithm("SHA256withRSA").build()

        val p521KeyPair = KeyPairGenerator.getInstance("EC").apply {
            initialize(ECGenParameterSpec("secp521r1"))
        }.generateKeyPair()

        val p521Root = TestCertificateGenerator(
            subject = "CN=Root,C=GB,ST=London",
            keyPair = p521KeyPair,
            issuerKeyPair = p521KeyPair,
            issuer = "CN=Root,C=GB,ST=London"
        ).ca().withSignatureAlgorithm("SHA512withECDSA").build()

        val p521Leaf = TestCertificateGenerator(
            subject = "CN=Leaf,C=GB,ST=London",
            keyPair = CertificateStubs.leafKeyPair,
            issuerKeyPair = p521KeyPair,
            issuer = "CN=Root,C=GB,ST=London"
        ).leaf().withSignatureAlgorithm("SHA512withECDSA").build()

        return listOf(
            UnsupportedAlgorithmCase(
                description = "RSA signing algorithm",
                chain = listOf(rsaLeaf),
                root = rsaRoot
            ),
            UnsupportedAlgorithmCase(
                description = "P-521 curve (outside P-256/P-384 allow-list)",
                chain = listOf(p521Leaf),
                root = p521Root
            )
        )
    }
}
