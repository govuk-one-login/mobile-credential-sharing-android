package uk.gov.onelogin.sharing.cryptoService.verifier

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.binding
import java.math.BigInteger
import java.security.AlgorithmParameters
import java.security.InvalidKeyException
import java.security.KeyFactory
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.ECPoint
import java.security.spec.ECPublicKeySpec
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.cryptoService.cbor.decodeDeviceEngagement
import uk.gov.onelogin.sharing.cryptoService.cbor.deriveSessionTranscript
import uk.gov.onelogin.sharing.cryptoService.cbor.deriveUntaggedCbor
import uk.gov.onelogin.sharing.cryptoService.cose.CoseKey
import uk.gov.onelogin.sharing.cryptoService.cose.toDto
import uk.gov.onelogin.sharing.cryptoService.cryptography.Constants.ELLIPTIC_CURVE_ALGORITHM
import uk.gov.onelogin.sharing.cryptoService.cryptography.Constants.ELLIPTIC_CURVE_PARAMETER_SPEC
import uk.gov.onelogin.sharing.cryptoService.secureArea.KeyPairGenerator
import uk.gov.onelogin.sharing.cryptoService.secureArea.secret.SharedSecretGenerator
import uk.gov.onelogin.sharing.cryptoService.secureArea.session.SessionKeyDerivationException
import uk.gov.onelogin.sharing.cryptoService.secureArea.session.SessionKeyGenerator
import uk.gov.onelogin.sharing.cryptoService.secureArea.session.SessionKeyGenerator.Companion.DeviceRole.HOLDER
import uk.gov.onelogin.sharing.cryptoService.secureArea.session.SessionKeyGenerator.Companion.DeviceRole.VERIFIER
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMapper
import uk.gov.onelogin.sharing.models.mdoc.cbor.serializers.EmbeddedCbor
import uk.gov.onelogin.sharing.models.mdoc.security.CoseKeyDto
import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionData
import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionDataDto
import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionDataDto.Companion.toDto
import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionDataStatus
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.SessionEstablishmentDto
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DocRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ReaderAuthenticationDto
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponse

@Suppress("TooManyFunctions")
@ContributesBinding(AppScope::class, binding = binding<VerifierCryptoService>())
class VerifierCryptoServiceImpl(
    private val logger: Logger,
    private val keyPairGenerator: KeyPairGenerator,
    private val sharedSecretGenerator: SharedSecretGenerator,
    private val sessionKeyGenerator: SessionKeyGenerator,
    private val encryptDeviceRequestUseCase: EncryptDeviceRequestUseCase,
    private val decryptDeviceResponseUseCase: DecryptDeviceResponseUseCase
) : VerifierCryptoService {

    @Suppress("LongMethod")
    override fun establishSession(
        qrCodeData: String,
        updateContext: (VerifierCryptoContext) -> VerifierCryptoContext
    ) {
        require(qrCodeData.isNotBlank()) {
            logger.error(
                logTag,
                "error constructing SessionTranscript array due to" +
                    " DeviceEngagementBytes is blank"
            )
            "DeviceEngagementBytes must not be blank"
        }

        val engagementData = decodeDeviceEngagement(qrCodeData, logger)
            ?: error(
                "error constructing SessionTranscript array due to malformed/invalid DeviceEngagementBytes"
            )

        val serviceUuid = engagementData.getFirstPeripheralServerModeUuid()
            ?: error("No service UUID in engagement data")

        val keyPair = keyPairGenerator.generateEcKeyPair(
            ELLIPTIC_CURVE_ALGORITHM,
            ELLIPTIC_CURVE_PARAMETER_SPEC
        ) ?: error("Failed to generate ephemeral key pair")

        val coseKey = CoseKey.generateCoseKey(keyPair.public as ECPublicKey, logger)
        val eReaderKeyTagged = EmbeddedCbor(coseKey.toDto().toCbor()).toCbor()

        val sessionTranscript = deriveSessionTranscript(
            cborBase64Url = qrCodeData,
            eReaderKeyTagged = eReaderKeyTagged,
            logger = logger
        )

        val sessionTranscriptBytes = EmbeddedCbor(sessionTranscript).toCbor()

        val eDevicePublicKey = engagementData.security.ephemeralPublicKey
            ?.toEcPublicKey()
            ?: throw IllegalArgumentException("Missing ephemeral public key in device engagement")

        logger.debug(logTag, "SessionTranscriptBytes constructed successfully")

        val sharedSecret = computeSharedSecret(
            eReaderPrivateKey = keyPair.private as ECPrivateKey,
            eDevicePublicKey = eDevicePublicKey
        )

        val skReader = deriveSessionKey(
            sharedSecret = sharedSecret,
            sessionTranscriptBytes = sessionTranscript,
            role = VERIFIER,
            label = "SKReader"
        )
        val skDevice = deriveSessionKey(
            sharedSecret = sharedSecret,
            sessionTranscriptBytes = sessionTranscript,
            role = HOLDER,
            label = "SKDevice"
        )

        updateContext(
            VerifierCryptoContext(
                serviceUuid = serviceUuid,
                eReaderKeyTagged = eReaderKeyTagged,
                sessionTranscriptBytes = sessionTranscriptBytes,
                eReaderKeyPair = keyPair,
                eDevicePublicKey = eDevicePublicKey,
                skReader = skReader,
                skDevice = skDevice
            )
        )
    }

    override fun buildReaderAuthenticationBytes(
        sessionTranscript: ByteArray,
        itemsRequestBytes: ByteArray
    ): ByteArray = try {
        val dto = ReaderAuthenticationDto(
            sessionTranscript = sessionTranscript,
            itemsRequestBytes = itemsRequestBytes
        )
        EmbeddedCbor(dto.toCbor()).toCbor().also {
            logger.debug(logTag, "ReaderAuthenticationBytes constructed successfully")
        }
    } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
        val message = "Error constructing ReaderAuthenticationBytes"
        logger.error(logTag, message, e)
        throw ReaderAuthenticationException(message, e)
    }

    override fun buildSessionEstablishment(
        eReaderKeyBytes: ByteArray,
        encryptedDeviceRequest: ByteArray
    ): ByteArray = try {
        SessionEstablishmentDto(
            eReaderKey = EmbeddedCbor(deriveUntaggedCbor(eReaderKeyBytes)),
            data = encryptedDeviceRequest
        ).toCbor().also {
            logger.debug(logTag, LOG_SESSION_ESTABLISHMENT_SUCCESS)
        }
    } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
        logger.error(logTag, LOG_SESSION_ESTABLISHMENT_ERROR, e)
        throw SessionEstablishmentException(LOG_SESSION_ESTABLISHMENT_ERROR, e)
    }

    override fun buildItemsRequestBytes(itemsRequest: ItemsRequest): ByteArray {
        val encoded = CborMapper.default.writeValueAsBytes(itemsRequest.toDto())
        return EmbeddedCbor(encoded).toCbor()
    }

    override fun buildDeviceRequest(
        itemsRequest: ItemsRequest,
        itemsRequestBytes: ByteArray?,
        readerAuth: ByteArray?
    ): ByteArray = DeviceRequest(
        version = "1.0",
        docRequests = listOf(
            DocRequest(
                itemsRequest = itemsRequest,
                readerAuth = readerAuth,
                itemsRequestBytes = itemsRequestBytes
            )
        )
    ).toDto().toCbor().also {
        logger.debug(logTag, "DeviceRequest bytes: ${it.toHexString()}")
    }

    override fun encryptDeviceRequest(
        deviceRequestBytes: ByteArray,
        skReader: ByteArray,
        encryptCounter: UInt
    ): ByteArray = encryptDeviceRequestUseCase(
        deviceRequestBytes = deviceRequestBytes,
        skReader = skReader,
        encryptCounter = encryptCounter
    )

    override fun deserializeSessionData(input: ByteArray): SessionData =
        CborMapper.default.readValue(input, SessionDataDto::class.java)
            .toDomain()

    override fun buildTerminationSessionData(): ByteArray =
        SessionData(status = SessionDataStatus.SESSION_TERMINATION)
            .toDto()
            .toCbor()

    override fun decryptDeviceResponse(
        deviceResponseBytes: ByteArray,
        skDevice: ByteArray,
        decryptCounter: UInt
    ): DeviceResponse = decryptDeviceResponseUseCase(
        deviceResponseBytes = deviceResponseBytes,
        skDevice = skDevice,
        encryptCounter = decryptCounter
    )

    private fun computeSharedSecret(
        eReaderPrivateKey: ECPrivateKey,
        eDevicePublicKey: ECPublicKey
    ): ByteArray {
        val deviceCurve = eDevicePublicKey.params.curve
        val readerCurve = eReaderPrivateKey.params.curve
        if (deviceCurve != readerCurve) {
            val message = "Error computing shared secret due to " +
                "EDeviceKey.Pub with incompatible curve: $deviceCurve"
            logger.error(logTag, message)
            throw SharedSecretException.IncompatibleCurve(deviceCurve.toString())
        }

        return try {
            sharedSecretGenerator.generateSharedSecret(
                thisDevicePrivateKey = eReaderPrivateKey,
                otherDevicePublicKey = eDevicePublicKey
            ).also {
                logger.debug(logTag, "Shared secret computed successfully")
            }
        } catch (e: InvalidKeyException) {
            logger.error(logTag, "Error computing shared secret due to malformed EDeviceKey.Pub")
            throw SharedSecretException.MalformedKey(e)
        }
    }

    private fun deriveSessionKey(
        sharedSecret: ByteArray,
        sessionTranscriptBytes: ByteArray,
        role: SessionKeyGenerator.Companion.DeviceRole,
        label: String
    ): ByteArray = try {
        sessionKeyGenerator.deriveSessionKey(
            sharedKey = sharedSecret,
            sessionTranscriptBytes = sessionTranscriptBytes,
            role = role
        ).also { logger.debug(logTag, "$label key generated") }
    } catch (e: SessionKeyDerivationException) {
        logger.error(logTag, "$label key derivation failed", e)
        throw e
    }

    private fun CoseKeyDto.toEcPublicKey(): ECPublicKey {
        val ecPoint = ECPoint(BigInteger(1, x), BigInteger(1, y))
        val params = AlgorithmParameters.getInstance(ELLIPTIC_CURVE_ALGORITHM).apply {
            init(ECGenParameterSpec(ELLIPTIC_CURVE_PARAMETER_SPEC))
        }
        val ecSpec = params.getParameterSpec(java.security.spec.ECParameterSpec::class.java)
        return KeyFactory.getInstance(ELLIPTIC_CURVE_ALGORITHM)
            .generatePublic(ECPublicKeySpec(ecPoint, ecSpec)) as ECPublicKey
    }

    internal companion object {
        const val LOG_SESSION_ESTABLISHMENT_SUCCESS = "SessionEstablishment message constructed"
        const val LOG_SESSION_ESTABLISHMENT_ERROR =
            "error constructing SessionEstablishment message"
    }
}
