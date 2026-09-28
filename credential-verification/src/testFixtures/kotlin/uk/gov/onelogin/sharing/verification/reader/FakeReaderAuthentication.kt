package uk.gov.onelogin.sharing.verification.reader

import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest

/**
 * Test double implementation of [ReaderAuthentication] for unit testing.
 */
class FakeReaderAuthentication(
    var resultToReturn: ReaderAuthenticationResult = ReaderAuthenticationResult.Unfulfillable,
    var exceptionToThrow: ReaderAuthenticationFailure? = null
) : ReaderAuthentication {

    var lastDeviceRequest: DeviceRequest? = null
    var lastTranscript: ByteArray? = null
    var lastSupportedDocumentTypes: List<String>? = null
    var authenticateCalls = 0

    var lastTrustedReaderCertificates: List<java.security.cert.X509Certificate>? = null

    override fun authenticateDeviceRequest(
        deviceRequest: DeviceRequest,
        sessionTranscriptBytes: ByteArray,
        supportedDocumentTypes: List<String>,
        trustedReaderCertificates: List<java.security.cert.X509Certificate>
    ): ReaderAuthenticationResult {
        authenticateCalls++
        lastDeviceRequest = deviceRequest
        lastTranscript = sessionTranscriptBytes
        lastSupportedDocumentTypes = supportedDocumentTypes
        lastTrustedReaderCertificates = trustedReaderCertificates

        exceptionToThrow?.let { throw it }

        return resultToReturn
    }
}
