package uk.gov.onelogin.sharing.verification.cose

import java.security.cert.X509Certificate

/**
 * Sealed class representing different types of COSE verification results.
 * Encapsulates specific data for each result type, removing the need for nullables.
 */
sealed class CoseVerificationResult {
    /** Successful result for chain-based verification with an attached payload. */
    data class Attached(val leafCertificate: X509Certificate, val payload: ByteArray) :
        CoseVerificationResult() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Attached
            if (leafCertificate != other.leafCertificate) return false
            if (!payload.contentEquals(other.payload)) return false
            return true
        }

        override fun hashCode(): Int {
            var result = leafCertificate.hashCode()
            result = 31 * result + payload.contentHashCode()
            return result
        }
    }

    /** Successful result for chain-based verification with a detached payload. */
    data class Detached(val leafCertificate: X509Certificate) : CoseVerificationResult()

    /** Successful result for key-based verification. */
    data object KeyBased : CoseVerificationResult()
}
