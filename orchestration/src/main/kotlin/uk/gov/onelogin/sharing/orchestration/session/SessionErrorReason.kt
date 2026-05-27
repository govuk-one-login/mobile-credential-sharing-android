package uk.gov.onelogin.sharing.orchestration.session

import uk.gov.onelogin.sharing.orchestration.prerequisites.MissingPrerequisite

sealed class SessionErrorReason {
    /**
     * State for when the system cannot process device engagement information from a
     * successfully scanned QR code.
     */
    data class CannotProcessEngagement(val qrCode: String) : SessionErrorReason()

    /**
     * State for when a successfully scanned QR code doesn't contain the Service UUID necessary to
     * connect with the holder device.
     */
    data object ServiceUuidNotFound : SessionErrorReason()
    data class UnrecoverableThrowable(val exception: Throwable) : SessionErrorReason()

    data class UnrecoverablePrerequisite(
        val unrecoverablePrerequisites: List<MissingPrerequisite>
    ) : SessionErrorReason(),
        Iterable<MissingPrerequisite> by unrecoverablePrerequisites {
        constructor(
            vararg unrecoverablePrerequisites: MissingPrerequisite
        ) : this(
            unrecoverablePrerequisites.toList()
        )
    }

    /**
     * State for when the app cannot establish a secure session due to missing
     * cryptographic context.
     */
    data object MissingCryptoContext : SessionErrorReason()

    data object CannotEncryptDeviceRequest : SessionErrorReason()

    data object CannotBuildSessionEstablishment : SessionErrorReason()

    data object CannotSendMessage : SessionErrorReason()

    /**
     * State for when there's an issue with obtaining a
     * [uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionData] instance from an updated
     * bluetooth characteristic event.
     */
    data object InvalidSessionDataPayload : SessionErrorReason()

    data object CannotDecryptDeviceResponse : SessionErrorReason()

    /**
     * The Holder encountered a general, decoding, or validation error while processing the
     * DeviceRequest. The [statusCode] indicates the specific error (10, 11, or 12).
     */
    data class DeviceRequestProcessingError(val statusCode: UInt) : SessionErrorReason()

    /**
     * The Holder returned a successful status (0) but the documents array is empty or missing.
     */
    data object DocumentNotReturned : SessionErrorReason()

    /**
     * State for when the app cannot process the provided QR code.
     *
     * @see uk.gov.onelogin.sharing.cryptoService.scanner.QrScanResult.Invalid
     */
    data class UnsupportedQrCodeFormat(val rawValue: String) : SessionErrorReason()
}
