package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse

import kotlinx.serialization.Serializable
import uk.gov.onelogin.sharing.verification.format.document.IssuerSigned

/**
 * @param nameSpaces Map of namespace to list of original Tag 24 encoded IssuerSignedItemBytes.
 *                   Each ByteArray is the exact bytes from the credential to preserve MSO hash integrity.
 * @param issuerAuth The untouched issuerAuth bytes (COSE_Sign1) from the raw credential.
 */
@Serializable
data class SharingIssuerSigned(
    override val nameSpaces: Map<String, List<ByteArray>>?,
    override val issuerAuth: ByteArray
) : IssuerSigned {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SharingIssuerSigned

        if (nameSpaces == null) {
            if (other.nameSpaces != null) return false
        } else {
            if (other.nameSpaces == null) return false
            nameSpaces.entries.forEach { (key, value) ->
                if (!other.nameSpaces.containsKey(key)) return false
                value.forEachIndexed { index, bytes ->
                    other.nameSpaces[key]?.get(index)?.let {
                        if (!bytes.contentEquals(it)) return false
                    } ?: return false
                }
            }
        }

        if (!issuerAuth.contentEquals(other.issuerAuth)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = nameSpaces?.hashCode() ?: 0
        result = 31 * result + issuerAuth.contentHashCode()
        return result
    }
}
