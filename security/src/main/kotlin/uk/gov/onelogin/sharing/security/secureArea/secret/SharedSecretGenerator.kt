package uk.gov.onelogin.sharing.security.secureArea.secret

import java.security.KeyPair
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey

fun interface SharedSecretGenerator {
    /**
     * Takes the holder's private key and the eReader's public key to compute a
     * common secret that can be used to derive symmetric keys for encrypting communication.
     *
     * @param holderKey The private key of the key holder.
     * @param eReaderKey The public key of the reader.
     * @return A [ByteArray] containing the computed shared secret.
     */
    fun generateSharedSecret(holderKey: ECPrivateKey, eReaderKey: ECPublicKey): ByteArray

    fun generateSharedSecret(keyPair: KeyPair) = generateSharedSecret(
        holderKey = keyPair.private as ECPrivateKey,
        eReaderKey = keyPair.public as ECPublicKey
    )
}
