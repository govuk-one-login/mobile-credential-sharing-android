package uk.gov.onelogin.sharing.verification.reader

import dev.zacsweers.metro.Inject
import java.security.cert.X509Certificate

private const val OID_SIA = "1.3.6.1.5.5.7.1.11"
private const val TAG_OCTET_STRING = 0x04
private const val TAG_URI_LOCATION = 0x86
private const val BYTE_MASK = 0xFF
private const val MAX_SEARCH_LOOKAHEAD = 20
private const val ASN1_HEADER_MIN_SIZE = 2
private const val SHORT_FORM_LEN_LIMIT = 128
private const val LONG_FORM_MASK = 0x7F

private const val OID_PREFIX_BYTE_0 = 0x2B.toByte()
private const val OID_PREFIX_BYTE_1 = 0x06.toByte()
private const val OID_PREFIX_BYTE_2 = 0x01.toByte()
private const val OID_PREFIX_BYTE_3 = 0x04.toByte()
private const val OID_PREFIX_BYTE_4 = 0x01.toByte()

private val OID_ENTERPRISE_PREFIX = byteArrayOf(
    OID_PREFIX_BYTE_0,
    OID_PREFIX_BYTE_1,
    OID_PREFIX_BYTE_2,
    OID_PREFIX_BYTE_3,
    OID_PREFIX_BYTE_4
)

/**
 * Pulls the DVS Privacy Policy URI out of the SIA extension's DER encoding.
 * Returns null on any malformed or missing data.
 */
@Inject
class SiaExtensionParser {

    fun extractPrivacyPolicyUrl(cert: X509Certificate): String? {
        val extValue = cert.getExtensionValue(OID_SIA) ?: return null
        return try {
            unwrapOctetStrings(extValue)?.let { findPrivacyPolicyUri(it) }
        } catch (@Suppress("TooGenericExceptionCaught") _: Exception) {
            null
        }
    }

    private fun unwrapOctetStrings(data: ByteArray): ByteArray? {
        val outer = parseAsn1OctetString(data) ?: return null
        val isNestedOctetString =
            outer.isNotEmpty() && (outer[0].toInt() and BYTE_MASK) == TAG_OCTET_STRING
        return if (isNestedOctetString) parseAsn1OctetString(outer) else outer
    }

    private fun findPrivacyPolicyUri(siaData: ByteArray): String? {
        val oidIndex = indexOfBytes(siaData, OID_ENTERPRISE_PREFIX)
        val start = oidIndex + OID_ENTERPRISE_PREFIX.size
        val limit = minOf(start + MAX_SEARCH_LOOKAHEAD, siaData.size - ASN1_HEADER_MIN_SIZE)

        return if (oidIndex >= 0 && start < limit) {
            (start until limit).firstNotNullOfOrNull { i ->
                if ((siaData[i].toInt() and BYTE_MASK) == TAG_URI_LOCATION) {
                    extractUriAtOffset(siaData, i)
                } else {
                    null
                }
            }
        } else {
            null
        }
    }

    private fun extractUriAtOffset(siaData: ByteArray, uriTagIndex: Int): String? {
        val len = siaData[uriTagIndex + 1].toInt() and BYTE_MASK
        val start = uriTagIndex + 2
        if (uriTagIndex + 1 >= siaData.size || start + len > siaData.size) return null
        return String(siaData, start, len, Charsets.UTF_8)
    }

    private fun indexOfBytes(source: ByteArray, target: ByteArray): Int {
        val matchIndex = (0..source.size - target.size).firstOrNull { i ->
            source.copyOfRange(i, i + target.size).contentEquals(target)
        }
        return matchIndex ?: -1
    }

    private fun parseAsn1OctetString(data: ByteArray): ByteArray? {
        if (data.size < ASN1_HEADER_MIN_SIZE ||
            (data[0].toInt() and BYTE_MASK) != TAG_OCTET_STRING
        ) {
            return null
        }
        val len = data[1].toInt() and BYTE_MASK
        val lenOffset = if (len < SHORT_FORM_LEN_LIMIT) {
            ASN1_HEADER_MIN_SIZE
        } else {
            ASN1_HEADER_MIN_SIZE + (len and LONG_FORM_MASK)
        }
        if (lenOffset > data.size) return null
        return data.copyOfRange(lenOffset, data.size)
    }
}
