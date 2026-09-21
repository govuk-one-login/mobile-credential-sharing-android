package uk.gov.onelogin.sharing.testapp.credential

import java.security.KeyFactory
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec

const val ALGORITHM_EC = "EC"
const val SIGNING_ALGORITHM = "SHA256withECDSA"

/**
 * Signs [payload] using the EC [privateKeyBytes] (raw PKCS#8) with [SIGNING_ALGORITHM].
 *
 * Shared by the Test App [uk.gov.onelogin.sharing.orchestration.CredentialProvider]
 * implementations. In a production app, signing would be delegated to the Android Keystore so the
 * private key never leaves secure hardware.
 */
internal fun signWithEcPrivateKey(payload: ByteArray, privateKeyBytes: ByteArray): ByteArray {
    val privateKey = KeyFactory.getInstance(ALGORITHM_EC)
        .generatePrivate(PKCS8EncodedKeySpec(privateKeyBytes))

    return Signature.getInstance(SIGNING_ALGORITHM).run {
        initSign(privateKey)
        update(payload)
        sign()
    }
}
