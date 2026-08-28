package uk.gov.onelogin.sharing.verification.cose.internal.decode

/**
 * Internal decoded representation of a COSE_Sign1 structure.
 * Prevents dependency on public format-domain models.
 */
internal data class InternalCoseSign1(
    val protectedHeader: ByteArray,
    val unprotectedHeader: ByteArray?,
    val payload: ByteArray?,
    val signature: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InternalCoseSign1

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
