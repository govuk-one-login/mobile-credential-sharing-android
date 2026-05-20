package uk.gov.onelogin.sharing.verification

/**
 * @param deviceKey The raw binary encoding of the device's COSE key.
 * @param keyAuthorizations Map of namespaces or data element authorizations. Defaults to null
 */
data class DeviceKeyInfo(
    val deviceKey: ByteArray,
    val keyAuthorizations: Map<String, Any>?
)
