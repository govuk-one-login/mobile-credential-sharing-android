package uk.gov.onelogin.sharing.verification.models

import uk.gov.onelogin.sharing.verification.result.VerificationError
import uk.gov.onelogin.sharing.verification.result.VerificationResult

/**
 * [MobileSecurityObject] (MSO) acts as a context object for verification.
 *
 * @param version The version of the schema.
 * @param digestAlgorithm The identifier of the digest algorithm used.
 * @param docType The document type string.
 * @param valueDigests [String] namespaces associated with a group of [Int] digest identifiers
 * and their relevant digest [ByteArray]s.
 * @param status Optional property stored as raw bytes. MSO-level status list revocation is
 * currently out of scope. Defaults to null, meaning that no additional validation occurs once
 * implemented.
 */
data class MobileSecurityObject(
    val version: String = MSO_SCHEMA_VERSION,
    val digestAlgorithm: String = MSO_DIGEST_ALGORITHM,
    val docType: String,
    val valueDigests: Map<String, Map<Int, ByteArray>>,
    val deviceKeyInfo: DeviceKeyInfo,
    val validityInfo: ValidityInfo,
    val status: ByteArray? = null,
) {

    init {
        if (MSO_SCHEMA_VERSION != version) {
            throw VerificationResult.Failure(
                VerificationError.INVALID_MSO_VERSION
            )
        }

        if (MSO_DIGEST_ALGORITHM != digestAlgorithm) {
            throw VerificationResult.Failure(
                VerificationError.UNSUPPORTED_DIGEST_ALGORITHM
            )
        }
    }

    fun hasDocType(expected: String): Boolean {
        if (expected != docType) {
            throw VerificationResult.Failure(
                VerificationError.INVALID_DOC_TYPE
            )
        }

        return true
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MobileSecurityObject

        if (version != other.version) return false
        if (digestAlgorithm != other.digestAlgorithm) return false
        if (docType != other.docType) return false
        if (valueDigests != other.valueDigests) return false
        if (deviceKeyInfo != other.deviceKeyInfo) return false
        if (validityInfo != other.validityInfo) return false
        if (!status.contentEquals(other.status)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = version.hashCode()
        result = 31 * result + digestAlgorithm.hashCode()
        result = 31 * result + docType.hashCode()
        result = 31 * result + valueDigests.hashCode()
        result = 31 * result + deviceKeyInfo.hashCode()
        result = 31 * result + validityInfo.hashCode()
        result = 31 * result + (status?.contentHashCode() ?: 0)
        return result
    }

    companion object {
        private const val MSO_SCHEMA_VERSION = "1.0"
        private const val MSO_DIGEST_ALGORITHM = "SHA-256"
    }
}
