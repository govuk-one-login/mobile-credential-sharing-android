package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse

import kotlinx.serialization.Serializable
import uk.gov.onelogin.sharing.verification.format.document.device.DeviceSigned

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
 * @property deviceNameSpacesBytes The Tag-24-wrapped DeviceNameSpaces CBOR bytes (empty for our implementation).
 * @property deviceSignature The DeviceAuth object containing the COSE_Sign1 deviceSignature.
 */
@Serializable
data class SharingDeviceSigned(
    override val deviceNameSpacesBytes: ByteArray,
    override val deviceSignature: ByteArray
) : DeviceSigned {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SharingDeviceSigned

        if (!deviceNameSpacesBytes.contentEquals(other.deviceNameSpacesBytes)) return false
        if (!deviceSignature.contentEquals(other.deviceSignature)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = deviceNameSpacesBytes.contentHashCode()
        result = 31 * result + deviceSignature.contentHashCode()
        return result
    }
}
