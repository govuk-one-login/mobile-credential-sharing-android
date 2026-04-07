package uk.gov.onelogin.sharing.cryptoService.verifier

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.binding
import java.math.BigInteger
import java.security.AlgorithmParameters
import java.security.KeyFactory
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

/**
 * Default implementation of [VerifierCryptoService].
 */
@ContributesBinding(AppScope::class, binding = binding<VerifierCryptoService>())
class VerifierCryptoServiceImpl(
    private val logger: Logger,
    private val keyPairGenerator: KeyPairGenerator
) : VerifierCryptoService {

    override fun processEngagement(
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

        updateContext(
            VerifierCryptoContext(
                engagementString = qrCodeData,
                serviceUuid = serviceUuid,
                eReaderKeyTagged = eReaderKeyTagged,
                sessionTranscriptBytes = sessionTranscriptBytes,
                eReaderKeyPair = keyPair,
                eDevicePublicKey = eDevicePublicKey
            )
        )

        logger.debug(logTag, "SessionTranscriptBytes constructed successfully")
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
