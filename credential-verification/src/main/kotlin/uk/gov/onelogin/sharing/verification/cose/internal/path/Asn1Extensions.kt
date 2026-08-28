package uk.gov.onelogin.sharing.verification.cose.internal.path

import java.security.cert.X509Certificate

private const val OID_SUBJECT_KEY_IDENTIFIER = "2.5.29.14"
private const val OID_AUTHORITY_KEY_IDENTIFIER = "2.5.29.35"

// ASN.1 DER tag identifiers
private const val TAG_OCTET_STRING = 0x04
private const val TAG_SEQUENCE = 0x30
private const val TAG_BIT_STRING = 0x03
private const val TAG_CONTEXT_SPECIFIC_0 = 0x80

// ASN.1 DER length encoding
private const val LONG_FORM_FLAG = 0x80
private const val LONG_FORM_LENGTH_MASK = 0x7F
private const val BYTE_MASK = 0xFF
private const val BITS_PER_BYTE = 8

private const val MIN_ASN1_SIZE = 2

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

/**
 * Extracts the raw public key bit string from a SubjectPublicKeyInfo DER structure.
 * SubjectPublicKeyInfo ::= SEQUENCE { AlgorithmIdentifier, BIT STRING }
 */
@Suppress("ReturnCount")
internal fun extractSubjectPublicKeyBits(spki: ByteArray): ByteArray? {
    if (spki.size < MIN_ASN1_SIZE) return null
    if (spki[0].toInt() and BYTE_MASK != TAG_SEQUENCE) return null
    var offset = 1
    val seqLen = asn1Length(spki, offset) ?: return null
    offset = seqLen.offset
    if (offset >= spki.size || spki[offset].toInt() and BYTE_MASK != TAG_SEQUENCE) return null
    offset++

    val algLen = asn1Length(spki, offset) ?: return null
    offset = algLen.offset + algLen.length

    if (offset >= spki.size || spki[offset].toInt() and BYTE_MASK != TAG_BIT_STRING) return null
    offset++
    val bitStringLen = asn1Length(spki, offset) ?: return null
    offset = bitStringLen.offset

    offset++
    return spki.copyOfRange(offset, offset + bitStringLen.length - 1)
}

internal fun ByteArray.toHexString(): String = joinToString("") { "%02x".format(it) }

private fun parseAsn1OctetString(data: ByteArray): ByteArray? {
    if (data.size < MIN_ASN1_SIZE) return null
    if (data[0].toInt() and BYTE_MASK != TAG_OCTET_STRING) return null
    val len = asn1Length(data, 1) ?: return null
    return data.copyOfRange(len.offset, len.offset + len.length)
}

private fun parseAkiKeyIdentifier(data: ByteArray): ByteArray? {
    if (data.size < MIN_ASN1_SIZE) return null
    if (data[0].toInt() and BYTE_MASK != TAG_SEQUENCE) return null
    val seqLen = asn1Length(data, 1) ?: return null
    if (seqLen.offset >= data.size) return null
    if (data[seqLen.offset].toInt() and BYTE_MASK != TAG_CONTEXT_SPECIFIC_0) return null
    val kidLen = asn1Length(data, seqLen.offset + 1) ?: return null
    return data.copyOfRange(kidLen.offset, kidLen.offset + kidLen.length)
}

private fun asn1Length(data: ByteArray, startIndex: Int): Asn1Length? {
    if (startIndex >= data.size) return null
    val first = data[startIndex].toInt() and BYTE_MASK
    return if (first < LONG_FORM_FLAG) {
        Asn1Length(length = first, offset = startIndex + 1)
    } else {
        val numBytes = first and LONG_FORM_LENGTH_MASK
        if (numBytes == 0 || startIndex + 1 + numBytes > data.size) return null
        var length = 0
        for (i in 0 until numBytes) {
            length = (length shl BITS_PER_BYTE) or (data[startIndex + 1 + i].toInt() and BYTE_MASK)
        }
        Asn1Length(length = length, offset = startIndex + 1 + numBytes)
    }
}

private data class Asn1Length(val length: Int, val offset: Int)
