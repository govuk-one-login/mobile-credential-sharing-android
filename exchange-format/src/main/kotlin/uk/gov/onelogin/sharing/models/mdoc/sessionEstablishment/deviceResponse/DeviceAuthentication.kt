package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse

/**
 * Represents the DeviceAuthentication structure as defined in ISO 18013-5.
 *
 * @property sessionTranscript The raw SessionTranscript CBOR bytes.
 * @property docType The document type string (e.g. "org.iso.18013.5.1.mDL").
 * @property deviceNameSpacesBytes The Tag-24-wrapped DeviceNameSpaces CBOR bytes.
 */
data class DeviceAuthentication(
    val sessionTranscript: ByteArray,
    val docType: String,
    val deviceNameSpacesBytes: ByteArray
) {
    val label: String = DEVICE_AUTHENTICATION

    companion object {
        const val DEVICE_AUTHENTICATION = "DeviceAuthentication"
        const val ELEMENT_COUNT = 4
    }
}
