package uk.gov.onelogin.sharing.orchestration.verifier.session

import uk.gov.onelogin.sharing.cryptoService.verifier.EncryptDeviceRequestException
import uk.gov.onelogin.sharing.orchestration.verificationrequest.VerificationRequest

class FakeBuildDeviceRequestUseCase(
    private val encryptedToReturn: ByteArray = byteArrayOf(0x01, 0x02),
    private val exceptionToThrow: EncryptDeviceRequestException? = null
) : BuildDeviceRequestUseCase {

    var lastVerificationRequest: VerificationRequest? = null
    var lastSkReader: ByteArray? = null
    var lastEncryptCounter: UInt? = null

    override fun execute(
        verificationRequest: VerificationRequest,
        skReader: ByteArray,
        encryptCounter: UInt
    ): ByteArray {
        lastVerificationRequest = verificationRequest
        lastSkReader = skReader
        lastEncryptCounter = encryptCounter
        exceptionToThrow?.let { throw it }
        return encryptedToReturn
    }
}
