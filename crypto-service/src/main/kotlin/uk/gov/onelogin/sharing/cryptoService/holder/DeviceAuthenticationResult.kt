package uk.gov.onelogin.sharing.cryptoService.holder

/**
 * Output of the DeviceAuthentication construction.
 *
 * @property deviceAuthenticationBytes The Tag-24-wrapped CBOR payload for signing.
 * @property deviceNameSpacesBytes The Tag-24-wrapped empty map.
 */
data class DeviceAuthenticationResult(
    val deviceAuthenticationBytes: ByteArray,
    val deviceNameSpacesBytes: ByteArray
)
