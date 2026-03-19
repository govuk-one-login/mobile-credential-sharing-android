package uk.gov.onelogin.sharing.cryptoService.cose

/**
 * Use case functional interface for converting a provided [CoseKey] into a string.
 */
fun interface CoseKeyToString {
    /**
     * @return A string representation of the provided [key].
     * @sample DefaultCoseKeyToString.convert
     */
    fun convert(key: CoseKey): String
}
