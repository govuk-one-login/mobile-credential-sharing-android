package uk.gov.onelogin.sharing.orchestration.verifier.session

import uk.gov.onelogin.sharing.cryptoService.verifier.EncryptDeviceRequestException
import uk.gov.onelogin.sharing.orchestration.verificationrequest.VerificationRequest

fun interface BuildDeviceRequestUseCase {
    /**
     * Constructs a [DeviceRequest] from the [verificationRequest], encodes it to CBOR,
     * and encrypts it using AES-256-GCM with the [skReader] session key.
     *
     * @throws EncryptDeviceRequestException if encryption fails.
     */
    @Throws(EncryptDeviceRequestException::class)
    fun execute(
        verificationRequest: VerificationRequest,
        skReader: ByteArray,
        encryptCounter: UInt
    ): ByteArray
}
