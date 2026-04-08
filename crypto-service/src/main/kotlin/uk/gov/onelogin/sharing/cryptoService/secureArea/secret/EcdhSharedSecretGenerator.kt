package uk.gov.onelogin.sharing.cryptoService.secureArea.secret

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import java.security.InvalidKeyException
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import javax.crypto.KeyAgreement
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.logger.logTag

@ContributesBinding(AppScope::class)
class EcdhSharedSecretGenerator(private val logger: Logger) : SharedSecretGenerator {
    /**
     * Generates a shared secret using the Elliptic Curve Diffie-Hellman (ECDH) key agreement
     * protocol.
     *
     * This method takes this device's private key and the other device's public key to compute a
     * common secret that can be used to derive symmetric keys for encrypting communication.
     *
     * @param thisDevicePrivateKey The private key of this device.
     * @param otherDevicePublicKey The public key of the other device.
     * @return A [ByteArray] containing the computed shared secret.
     * @throws java.security.InvalidKeyException if the provided keys are invalid or incompatible for ECDH,
     *         wrapping the original exception.
     */
    override fun generateSharedSecret(
        thisDevicePrivateKey: ECPrivateKey,
        otherDevicePublicKey: ECPublicKey
    ): ByteArray {
        try {
            val keyAgreement = KeyAgreement.getInstance("ECDH")
            keyAgreement.init(thisDevicePrivateKey)
            keyAgreement.doPhase(otherDevicePublicKey, true)
            return keyAgreement.generateSecret()
        } catch (e: InvalidKeyException) {
            logger.debug(logTag, "Unable to create shared secret (status 10): $e")
            throw InvalidKeyException("Unexpected Exception", e)
        }
    }
}
