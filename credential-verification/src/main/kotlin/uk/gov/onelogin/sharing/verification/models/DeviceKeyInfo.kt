package uk.gov.onelogin.sharing.verification.models

/**
 * @param deviceKey The raw binary encoding of the device's COSE key.
 * @param keyAuthorizations Map of namespaces or data element authorizations. Defaults to null
 */
data class DeviceKeyInfo(
    val deviceKey: ByteArray,
    val keyAuthorizations: Map<String, String>? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DeviceKeyInfo

        if (!deviceKey.contentEquals(other.deviceKey)) return false
        if (keyAuthorizations != other.keyAuthorizations) return false

        return true
    }

    override fun hashCode(): Int {
        var result = deviceKey.contentHashCode()
        result = 31 * result + (keyAuthorizations?.hashCode() ?: 0)
        return result
    }
}
