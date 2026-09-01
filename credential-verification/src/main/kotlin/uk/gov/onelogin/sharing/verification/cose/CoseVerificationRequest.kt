package uk.gov.onelogin.sharing.verification.cose

import java.security.cert.X509Certificate
import java.security.interfaces.ECPublicKey

/**
 * Sealed class representing different types of COSE verification requests.
 */
sealed class CoseVerificationRequest {
    /** Chain-based, attached payload (IssuerAuth). */
    data class Attached(val coseSign1Bytes: ByteArray, val trustedRoot: X509Certificate) :
        CoseVerificationRequest() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Attached
            if (!coseSign1Bytes.contentEquals(other.coseSign1Bytes)) return false
            if (trustedRoot != other.trustedRoot) return false
            return true
        }

        override fun hashCode(): Int {
            var result = coseSign1Bytes.contentHashCode()
            result = 31 * result + trustedRoot.hashCode()
            return result
        }
    }

    /** Chain-based, detached payload (ReaderAuth). */
    data class Detached(
        val coseSign1Bytes: ByteArray,
        val detachedPayload: ByteArray,
        val trustedRoot: X509Certificate
    ) : CoseVerificationRequest() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Detached
            if (!coseSign1Bytes.contentEquals(other.coseSign1Bytes)) return false
            if (!detachedPayload.contentEquals(other.detachedPayload)) return false
            if (trustedRoot != other.trustedRoot) return false
            return true
        }

        override fun hashCode(): Int {
            var result = coseSign1Bytes.contentHashCode()
            result = 31 * result + detachedPayload.contentHashCode()
            result = 31 * result + trustedRoot.hashCode()
            return result
        }
    }

    /** Key-based, detached payload (DeviceSignature). */
    data class KeyBased(
        val coseSign1Bytes: ByteArray,
        val detachedPayload: ByteArray,
        val publicKey: ECPublicKey
    ) : CoseVerificationRequest() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as KeyBased
            if (!coseSign1Bytes.contentEquals(other.coseSign1Bytes)) return false
            if (!detachedPayload.contentEquals(other.detachedPayload)) return false
            if (publicKey != other.publicKey) return false
            return true
        }

        override fun hashCode(): Int {
            var result = coseSign1Bytes.contentHashCode()
            result = 31 * result + detachedPayload.contentHashCode()
            result = 31 * result + publicKey.hashCode()
            return result
        }
    }
}
