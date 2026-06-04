package uk.gov.onelogin.sharing.verification.format.cose

/**
 * Represents a decoded COSE_Sign1 structure.
 *
 * This is a pure data class with no parsing logic. Decoding from CBOR
 * is the responsibility of the consuming module.
 *
 * @param protectedHeader The serialised protected header bytes.
 * @param unprotectedHeader The serialised unprotected header bytes (null when empty).
 * @param payload The payload bytes, or null for detached content.
 * @param signature The raw signature bytes.
 */
data class CoseSign1(
    val protectedHeader: ByteArray,
    val unprotectedHeader: ByteArray?,
    val payload: ByteArray?,
    val signature: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CoseSign1

        if (!protectedHeader.contentEquals(other.protectedHeader)) return false
        if (!unprotectedHeader.contentEquals(other.unprotectedHeader)) return false
        if (!payload.contentEquals(other.payload)) return false
        if (!signature.contentEquals(other.signature)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = protectedHeader.contentHashCode()
        result = 31 * result + (unprotectedHeader?.contentHashCode() ?: 0)
        result = 31 * result + (payload?.contentHashCode() ?: 0)
        result = 31 * result + signature.contentHashCode()
        return result
    }
}
