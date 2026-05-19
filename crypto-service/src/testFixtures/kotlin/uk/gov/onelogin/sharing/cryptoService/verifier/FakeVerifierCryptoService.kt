package uk.gov.onelogin.sharing.cryptoService.verifier

import java.security.interfaces.ECPublicKey
import java.util.UUID
import uk.gov.onelogin.sharing.cryptoService.cbor.CborMapper
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.SessionDataDto
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.SessionDataDtoStubs.validSessionDataDtoBytes
import uk.gov.onelogin.sharing.cryptoService.secureArea.keypair.KeyPairGeneratorStubs.validKeyPair
import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionData
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequest

class FakeVerifierCryptoService : VerifierCryptoService {
    var establishSessionCallCount = 0
        private set
    var lastQrCodeData: String? = null
        private set
    var exceptionToThrow: Exception? = null
    var sessionKeysToReturn: Pair<ByteArray, ByteArray> =
        Pair(ByteArray(32), ByteArray(32))
    var buildAndEncryptToReturn: ByteArray = byteArrayOf(0x01, 0x02)
    var buildAndEncryptException: EncryptDeviceRequestException? = null
    var lastDeviceRequestBytes: ByteArray? = null
    var lastSkReader: ByteArray? = null
    var lastEncryptCounter: UInt? = null
    var buildSessionEstablishmentToReturn: ByteArray = byteArrayOf(0x03, 0x04)
    var buildSessionEstablishmentException: SessionEstablishmentException? = null
    var lastEReaderKeyBytes: ByteArray? = null
    var lastEncryptedDeviceRequest: ByteArray? = null
    var sessionData: SessionData = CborMapper.default.readValue(
        validSessionDataDtoBytes,
        SessionDataDto::class.java
    ).toDomain()

    override fun establishSession(
        qrCodeData: String,
        updateContext: (VerifierCryptoContext) -> VerifierCryptoContext
    ) {
        establishSessionCallCount++
        lastQrCodeData = qrCodeData
        exceptionToThrow?.let { throw it }
        DeferredVerifierCryptoService(
            updater = { qrCodeData ->
                VerifierCryptoContext(
                    engagementString = qrCodeData,
                    serviceUuid = UUID.randomUUID(),
                    eReaderKeyTagged = byteArrayOf(),
                    sessionTranscriptBytes = byteArrayOf(),
                    eReaderKeyPair = validKeyPair!!,
                    eDevicePublicKey = validKeyPair.public as ECPublicKey,
                    skReader = sessionKeysToReturn.first,
                    skDevice = sessionKeysToReturn.second
                )
            }
        ).establishSession(qrCodeData, updateContext)
    }

    override fun buildDeviceRequest(itemsRequest: ItemsRequest): ByteArray = byteArrayOf(0x01, 0x02)

    override fun encryptDeviceRequest(
        deviceRequestBytes: ByteArray,
        skReader: ByteArray,
        encryptCounter: UInt
    ): ByteArray {
        lastDeviceRequestBytes = deviceRequestBytes
        lastSkReader = skReader
        lastEncryptCounter = encryptCounter
        buildAndEncryptException?.let { throw it }
        return buildAndEncryptToReturn
    }

    override fun buildSessionEstablishment(
        eReaderKeyBytes: ByteArray,
        encryptedDeviceRequest: ByteArray
    ): ByteArray {
        lastEReaderKeyBytes = eReaderKeyBytes
        lastEncryptedDeviceRequest = encryptedDeviceRequest
        buildSessionEstablishmentException?.let { throw it }
        return buildSessionEstablishmentToReturn
    }

    override fun deserializeSessionData(input: ByteArray): SessionData {
        exceptionToThrow?.let { throw it }
        return sessionData
    }
}
