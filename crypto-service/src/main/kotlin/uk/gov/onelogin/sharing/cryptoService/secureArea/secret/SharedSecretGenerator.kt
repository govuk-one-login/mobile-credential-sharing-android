package uk.gov.onelogin.sharing.cryptoService.secureArea.secret

import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey

fun interface SharedSecretGenerator {
    /**
     * Takes the private key from this device and the public key from the device we're communicating
     * with to compute a common secret that can be used to derive symmetric keys for encrypting
     * communications.
     *
     * @param thisDevicePrivateKey The private key of this device
     * @param otherDevicePublicKey The public key of the other device
     * @return A [ByteArray] containing the computed shared secret.
     */
    fun generateSharedSecret(
        thisDevicePrivateKey: ECPrivateKey,
        otherDevicePublicKey: ECPublicKey
    ): ByteArray
}
