package uk.gov.onelogin.sharing.verification.cose.internal.decode

/**
 * Output of the C4 certificate-header validation stage.
 *
 * Carries the thumbprint-bound candidate signing leaf and the complete supplied
 * `x5chain` sequence in its original, unmodified order.
 *
 * @property candidateLeaf The DER bytes of the first supplied `x5chain` certificate.
 * @property chain The complete supplied `x5chain` sequence in supplied (leaf-first) order.
 */
internal data class CertificateHeaderProfile(
    val candidateLeaf: ByteArray,
    val chain: List<ByteArray>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CertificateHeaderProfile

        if (!candidateLeaf.contentEquals(other.candidateLeaf)) return false
        if (chain.size != other.chain.size) return false
        return chain.indices.all { chain[it].contentEquals(other.chain[it]) }
    }

    override fun hashCode(): Int {
        var result = candidateLeaf.contentHashCode()
        result = 31 * result + chain.fold(0) { acc, bytes -> 31 * acc + bytes.contentHashCode() }
        return result
    }
}
