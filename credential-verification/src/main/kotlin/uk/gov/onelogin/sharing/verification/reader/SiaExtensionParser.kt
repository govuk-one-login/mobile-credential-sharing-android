package uk.gov.onelogin.sharing.verification.reader

import dev.zacsweers.metro.Inject
import java.security.cert.X509Certificate

private const val OID_SIA = "1.3.6.1.5.5.7.1.11"
private const val TAG_OCTET_STRING = 0x04
private const val TAG_URI_LOCATION = 0x86
private const val BYTE_MASK = 0xFF
private const val ASN1_HEADER_MIN_SIZE = 2
private const val SHORT_FORM_LEN_LIMIT = 128
private const val LONG_FORM_MASK = 0x7F

private const val OID_TAG_BYTE = 0x06.toByte()
private const val OID_LENGTH_BYTE = 0x0A.toByte()
private const val OID_BYTE_0 = 0x2B.toByte()
private const val OID_BYTE_1 = 0x06.toByte()
private const val OID_BYTE_2 = 0x01.toByte()
private const val OID_BYTE_3 = 0x04.toByte()
private const val OID_BYTE_4 = 0x01.toByte()
private const val OID_BYTE_5 = 0x84.toByte()
private const val OID_BYTE_6 = 0x87.toByte()
private const val OID_BYTE_7 = 0x7F.toByte()
private const val OID_BYTE_8 = 0x01.toByte()
private const val OID_BYTE_9 = 0x01.toByte()

// Full DER byte sequence for OID 1.3.6.1.4.1.66559.1.1
private val OID_PRIVACY_POLICY_DER = byteArrayOf(
    OID_TAG_BYTE,
    OID_LENGTH_BYTE,
    OID_BYTE_0,
    OID_BYTE_1,
    OID_BYTE_2,
    OID_BYTE_3,
    OID_BYTE_4,
    OID_BYTE_5,
    OID_BYTE_6,
    OID_BYTE_7,
    OID_BYTE_8,
    OID_BYTE_9
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
        val oidIndex = indexOfBytes(siaData, OID_PRIVACY_POLICY_DER)
        if (oidIndex < 0) return null

        val uriTagIndex = oidIndex + OID_PRIVACY_POLICY_DER.size
        return extractUriAtOffset(siaData, uriTagIndex)
    }

    private fun extractUriAtOffset(siaData: ByteArray, uriTagIndex: Int): String? {
        if (uriTagIndex + 1 >= siaData.size) return null
        if ((siaData[uriTagIndex].toInt() and BYTE_MASK) != TAG_URI_LOCATION) return null

        val len = siaData[uriTagIndex + 1].toInt() and BYTE_MASK
        val start = uriTagIndex + 2
        return if (start + len > siaData.size) null else String(siaData, start, len, Charsets.UTF_8)
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
