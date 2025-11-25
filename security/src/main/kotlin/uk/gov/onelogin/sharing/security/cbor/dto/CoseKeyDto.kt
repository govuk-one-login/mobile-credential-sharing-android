package uk.gov.onelogin.sharing.security.cbor.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class CoseKeyDto(
    @JsonProperty("1")
    val keyType: Long,
    @JsonProperty("-1")
    val curve: Long,
    @JsonProperty("-2")
    val x: ByteArray,
    @JsonProperty("-3")
    val y: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CoseKeyDto

        if (keyType != other.keyType) return false
        if (curve != other.curve) return false
        if (!x.contentEquals(other.x)) return false
        if (!y.contentEquals(other.y)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = keyType.hashCode()
        result = 31 * result + curve.hashCode()
        result = 31 * result + x.contentHashCode()
        result = 31 * result + y.contentHashCode()
        return result
    }
}
