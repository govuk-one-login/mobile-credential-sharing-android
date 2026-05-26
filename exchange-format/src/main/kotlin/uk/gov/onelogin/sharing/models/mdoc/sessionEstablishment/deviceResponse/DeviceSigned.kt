package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse

/**
 * Represents the DeviceSigned structure as defined in ISO 18013-5.
 *
 * ```
 * DeviceSigned = {
 *   "nameSpaces" : DeviceNameSpacesBytes,
 *   "deviceAuth" : DeviceAuth
 * }
 * ```
 *
 * @property nameSpaces The Tag-24-wrapped DeviceNameSpaces CBOR bytes (empty for our implementation).
 * @property deviceAuth The DeviceAuth object containing the COSE_Sign1 deviceSignature.
 */
data class DeviceSigned(val nameSpaces: ByteArray, val deviceAuth: ByteArray)
