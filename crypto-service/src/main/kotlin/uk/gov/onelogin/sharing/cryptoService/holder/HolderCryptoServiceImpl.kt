package uk.gov.onelogin.sharing.cryptoService.holder

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.binding
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.SessionDataDto.Companion.toDto
import uk.gov.onelogin.sharing.cryptoService.cbor.encodeCbor
import uk.gov.onelogin.sharing.cryptoService.cbor.encodeDeviceNameSpacesBytes
import uk.gov.onelogin.sharing.cryptoService.cbor.toDto
import uk.gov.onelogin.sharing.cryptoService.secureArea.SessionSecurity
import uk.gov.onelogin.sharing.cryptoService.secureArea.session.SessionKeyGenerator.Companion.DeviceRole
import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionData
import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionDataStatus
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceAuthentication
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponse
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.Status
import uk.gov.onelogin.sharing.verification.format.document.VerifiableDocument

@ContributesBinding(scope = AppScope::class, binding = binding<HolderCryptoService>())
class HolderCryptoServiceImpl(
    private val sessionSecurity: SessionSecurity,
    private val logger: Logger
) : HolderCryptoService {
    override fun buildTerminationSessionData(status: SessionDataStatus): ByteArray =
        SessionData(status = status).toDto().toCbor()

    override fun buildErrorSessionData(
        deviceResponseStatus: Status,
        sessionDataStatus: SessionDataStatus,
        skDevice: ByteArray,
        encryptCounter: UInt
    ): ByteArray {
        val encryptedPayload = encryptDeviceResponse(
            deviceResponse = DeviceResponse(
                status = deviceResponseStatus
            ),
            skDevice = skDevice,
            encryptCounter = encryptCounter
        )
        return SessionData(data = encryptedPayload, status = sessionDataStatus)
            .toDto()
            .toCbor()
    }

    override fun buildDeviceResponse(
        documents: List<VerifiableDocument.WithPresentation>,
        skDevice: ByteArray,
        encryptCounter: UInt
    ): ByteArray {
        val encryptedPayload = encryptDeviceResponse(
            deviceResponse = DeviceResponse(
                documents = documents
            ),
            skDevice = skDevice,
            encryptCounter = encryptCounter
        )
        return SessionData(data = encryptedPayload).toDto().toCbor()
    }

    private fun encryptDeviceResponse(
        deviceResponse: DeviceResponse,
        skDevice: ByteArray,
        encryptCounter: UInt
    ): ByteArray {
        val cborBytes = deviceResponse.toDto().encodeCbor()

        logger.debug(logTag, "DeviceResponse encoded to ${cborBytes.size} CBOR bytes")

        return sessionSecurity.encryptPayload(
            key = skDevice,
            data = cborBytes,
            role = DeviceRole.HOLDER,
            encryptCounter = encryptCounter
        )
    }

    override fun buildDeviceAuthenticationBytes(
        sessionTranscript: ByteArray,
        docType: String
    ): DeviceAuthenticationResult {
        val deviceNameSpacesBytes = encodeDeviceNameSpacesBytes()

        logger.debug(
            logTag,
            "DeviceNameSpacesBytes encoded: ${deviceNameSpacesBytes.toHexString()}"
        )

        val deviceAuthenticationBytes = DeviceAuthentication(
            sessionTranscript = sessionTranscript,
            docType = docType,
            deviceNameSpacesBytes = deviceNameSpacesBytes
        ).encodeCbor()

        logger.debug(
            logTag,
            "DeviceAuthenticationBytes encoded: ${deviceAuthenticationBytes.toHexString()}"
        )

        return DeviceAuthenticationResult(
            deviceAuthenticationBytes = deviceAuthenticationBytes,
            deviceNameSpacesBytes = deviceNameSpacesBytes
        )
    }
}
