package uk.gov.onelogin.sharing.models.mdoc.cbor.serializers

import uk.gov.onelogin.sharing.models.mdoc.cbor.CborEncodable

/**
 * Represents a CBOR-encoded data item wrapped in Tag 24.
 *
 * Use [toCbor] to serialize (wrap in Tag 24 via [EmbeddedCborSerializer]),
 * or [EmbeddedCborDeserializer.unwrap] to deserialize (extract from Tag 24).
 */
data class EmbeddedCbor(val encoded: ByteArray) : CborEncodable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EmbeddedCbor

        return encoded.contentEquals(other.encoded)
    }

    override fun hashCode(): Int = encoded.contentHashCode()
}
