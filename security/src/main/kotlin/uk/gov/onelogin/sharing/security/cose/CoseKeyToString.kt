package uk.gov.onelogin.sharing.security.cose

/**
 * Use case functional interface for converting a provided [CoseKey] into a string.
 */
fun interface CoseKeyToString {
    fun convert(key: CoseKey): String
}