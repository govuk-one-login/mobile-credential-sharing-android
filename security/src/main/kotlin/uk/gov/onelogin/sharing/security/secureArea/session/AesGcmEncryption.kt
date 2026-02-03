package uk.gov.onelogin.sharing.security.secureArea.session

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metrox.viewmodel.ViewModelScope
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.security.cryptography.Constants.AES_256_ALGORITHM
import uk.gov.onelogin.sharing.security.cryptography.Constants.AES_256_NONCE_LENGTH
import uk.gov.onelogin.sharing.security.cryptography.Constants.AES_256_TRANSFORMATION
import uk.gov.onelogin.sharing.security.cryptography.createNistInitialisationVector
import uk.gov.onelogin.sharing.security.secureArea.session.SessionKeyGenerator.Companion.DeviceRole
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

@ContributesBinding(ViewModelScope::class)
class AesGcmEncryption(private val logger: Logger) : SessionEncryption {

    private var decryptionCounter = 1

    /**
     * Decrypt a "data" payload from a SessionEstablishment or SessionData message
     *
     * Generates a 12 byte nonce from a nist initialisation vector, using the device
     * role and a counter of the number of times that the decryption service has been
     * used.
     *
     * Decrypts the data using the nist initialisation vector and the generated HKDF
     * session key with the role of the sender
     *
     * @return [ByteArray] object representing the plaintext decrypted response
     */

    override fun decryptPayload(
        key: ByteArray,
        data: ByteArray,
        role: DeviceRole
    ): ByteArray {
        val nistInitialisationVector = createNistInitialisationVector(
            role.nistInitialisationVectorIdentifier,
            decryptionCounter.toUInt()
        )

        try {
            val decryptedData = Cipher.getInstance(AES_256_TRANSFORMATION).run {
                init(
                    Cipher.DECRYPT_MODE,
                    SecretKeySpec(
                        key,
                        AES_256_ALGORITHM
                    ),
                    GCMParameterSpec(
                        AES_256_NONCE_LENGTH,
                        nistInitialisationVector
                    )
                )
                doFinal(
                    data
                )
            }
            decryptionCounter += 1


//            logger.
            return decryptedData
        } catch (e: Exception) {
            // logger
            throw Exception()
        }
    }
}