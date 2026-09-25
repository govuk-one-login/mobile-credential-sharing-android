package uk.gov.onelogin.sharing.verification.reader

import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest

/**
 * Test double implementation of [ReaderAuthentication] for unit testing.
 */
class FakeReaderAuthentication(
    var resultToReturn: ReaderAuthenticationOutcome = ReaderAuthenticationOutcome.Unfulfillable,
    var exceptionToThrow: ReaderAuthenticationFailure? = null
) : ReaderAuthentication {

    var lastDeviceRequest: DeviceRequest? = null
    var lastTranscript: ByteArray? = null
    var lastSupportedDocumentTypes: List<String>? = null
    var authenticateCalls = 0

    override fun authenticateDeviceRequest(
        deviceRequest: DeviceRequest,
        untaggedSessionTranscriptBytes: ByteArray,
        supportedDocumentTypes: List<String>
    ): ReaderAuthenticationOutcome {
        authenticateCalls++
        lastDeviceRequest = deviceRequest
        lastTranscript = untaggedSessionTranscriptBytes
        lastSupportedDocumentTypes = supportedDocumentTypes

        exceptionToThrow?.let { throw it }

        return resultToReturn
    }
}
