package uk.gov.onelogin.sharing.verification.trust

import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.cert.X509Certificate
import java.security.spec.ECGenParameterSpec

internal object CertTestHelpers {
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
}
