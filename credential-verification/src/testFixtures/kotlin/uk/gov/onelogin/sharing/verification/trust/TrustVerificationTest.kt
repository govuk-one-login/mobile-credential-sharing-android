package uk.gov.onelogin.sharing.verification.trust

import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.cert.X509Certificate
import java.security.spec.ECGenParameterSpec
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertThrows
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResultMatchers.hasError

interface TrustVerificationTest {
    val verifier: TrustVerifier

    fun generateKeyPair(): KeyPair {
        val gen = KeyPairGenerator.getInstance("EC")
        gen.initialize(ECGenParameterSpec("secp256r1"))
        return gen.generateKeyPair()
    }

    fun buildCoseSign1WithChain(chain: List<X509Certificate>, leafKp: KeyPair): ByteArray =
        CoseSign1Builder.build(
            chain = chain,
            leafKeyPair = leafKp,
            payload = MsoBuilder.build(
                validFrom = chain.first().notBefore,
                validUntil = chain.first().notAfter
            )
        )

    fun assertVerificationFailure(
        chain: List<X509Certificate>,
        leafKp: KeyPair,
        root: X509Certificate,
        expectedError: VerificationError
    ) {
        val coseSign1 = buildCoseSign1WithChain(chain, leafKp)
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            verifier.verifyCOSESign1(coseSign1, root)
        }
        assertThat(exception, hasError(expectedError))
    }
}
