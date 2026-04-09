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
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.CoseKeyDto
import uk.gov.onelogin.sharing.cryptoService.cbor.encodeCbor
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.EmbeddedCbor
import uk.gov.onelogin.sharing.cryptoService.cose.CoseKey
import uk.gov.onelogin.sharing.cryptoService.cryptography.Constants.ELLIPTIC_CURVE_ALGORITHM
import uk.gov.onelogin.sharing.cryptoService.cryptography.Constants.ELLIPTIC_CURVE_PARAMETER_SPEC
import uk.gov.onelogin.sharing.cryptoService.secureArea.KeyPairGenerator
import uk.gov.onelogin.sharing.cryptoService.secureArea.secret.SharedSecretGenerator
import uk.gov.onelogin.sharing.cryptoService.secureArea.session.SessionKeyDerivationException
import uk.gov.onelogin.sharing.cryptoService.secureArea.session.SessionKeyGenerator
import uk.gov.onelogin.sharing.cryptoService.secureArea.session.SessionKeyGenerator.Companion.DeviceRole.HOLDER
import uk.gov.onelogin.sharing.cryptoService.secureArea.session.SessionKeyGenerator.Companion.DeviceRole.VERIFIER

@ContributesBinding(AppScope::class, binding = binding<VerifierCryptoService>())
class VerifierCryptoServiceImpl(
    private val logger: Logger,
    private val keyPairGenerator: KeyPairGenerator,
    private val sharedSecretGenerator: SharedSecretGenerator,
    private val sessionKeyGenerator: SessionKeyGenerator
) : VerifierCryptoService {

    override fun establishSession(
        qrCodeData: String,
        updateContext: (VerifierCryptoContext) -> VerifierCryptoContext
    ) {
        require(qrCodeData.isNotBlank()) {
            logger.error(
                logTag,
                "error constructing SessionTranscript array due to DeviceEngagementBytes is blank"
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
        val eReaderKeyTagged = EmbeddedCbor(coseKey.encodeCbor()).encodeCbor()

        val sessionTranscript = deriveSessionTranscript(
            cborBase64Url = qrCodeData,
            eReaderKeyTagged = eReaderKeyTagged,
            logger = logger
        )

        val sessionTranscriptBytes = EmbeddedCbor(sessionTranscript).encodeCbor()

        val eDevicePublicKey = engagementData.security.ephemeralPublicKey
            .toEcPublicKey()

        logger.debug(logTag, "SessionTranscriptBytes constructed successfully")

        val sharedSecret = computeSharedSecret(
            eReaderPrivateKey = keyPair.private as ECPrivateKey,
            eDevicePublicKey = eDevicePublicKey
        )

        val skReader = deriveSessionKey(
            sharedSecret = sharedSecret,
            sessionTranscriptBytes = sessionTranscriptBytes,
            role = VERIFIER,
            label = "SKReader"
        )
        val skDevice = deriveSessionKey(
            sharedSecret = sharedSecret,
            sessionTranscriptBytes = sessionTranscriptBytes,
            role = HOLDER,
            label = "SKDevice"
        )

        updateContext(
            VerifierCryptoContext(
                engagementString = qrCodeData,
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
}
