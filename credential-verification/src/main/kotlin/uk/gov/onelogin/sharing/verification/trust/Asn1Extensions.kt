package uk.gov.onelogin.sharing.verification.trust

import java.security.cert.X509Certificate

private const val OID_SUBJECT_KEY_IDENTIFIER = "2.5.29.14"
private const val OID_AUTHORITY_KEY_IDENTIFIER = "2.5.29.35"

internal fun X509Certificate.subjectKeyIdentifierHex(): String? {
    val ext = getExtensionValue(OID_SUBJECT_KEY_IDENTIFIER) ?: return null
    val outer = parseAsn1OctetString(ext) ?: return null
    val inner = parseAsn1OctetString(outer) ?: return null
    return inner.toHexString()
}

internal fun X509Certificate.authorityKeyIdentifierHex(): String? {
    val ext = getExtensionValue(OID_AUTHORITY_KEY_IDENTIFIER) ?: return null
    val outer = parseAsn1OctetString(ext) ?: return null
    val keyId = parseAkiKeyIdentifier(outer) ?: return null
    return keyId.toHexString()
}

private fun parseAsn1OctetString(data: ByteArray): ByteArray? {
    if (data.size < 2) return null
    if (data[0].toInt() and 0xFF != 0x04) return null
    val len = asn1Length(data, 1) ?: return null
    return data.copyOfRange(len.offset, len.offset + len.length)
}

private fun parseAkiKeyIdentifier(data: ByteArray): ByteArray? {
    if (data.size < 2) return null
    if (data[0].toInt() and 0xFF != 0x30) return null
    val seqLen = asn1Length(data, 1) ?: return null
    if (seqLen.offset >= data.size) return null
    if (data[seqLen.offset].toInt() and 0xFF != 0x80) return null
    val kidLen = asn1Length(data, seqLen.offset + 1) ?: return null
    return data.copyOfRange(kidLen.offset, kidLen.offset + kidLen.length)
}

private fun asn1Length(data: ByteArray, startIndex: Int): Asn1Length? {
    if (startIndex >= data.size) return null
    val first = data[startIndex].toInt() and 0xFF
    return if (first < 0x80) {
        Asn1Length(length = first, offset = startIndex + 1)
    } else {
        val numBytes = first and 0x7F
        if (numBytes == 0 || startIndex + 1 + numBytes > data.size) return null
        var length = 0
        for (i in 0 until numBytes) {
            length = (length shl 8) or (data[startIndex + 1 + i].toInt() and 0xFF)
        }
        Asn1Length(length = length, offset = startIndex + 1 + numBytes)
    }
}

private data class Asn1Length(val length: Int, val offset: Int)

private fun ByteArray.toHexString(): String =
    joinToString("") { "%02x".format(it) }
