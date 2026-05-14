package uk.gov.onelogin.sharing.cryptoService.cbor

import com.fasterxml.jackson.databind.ObjectMapper

interface CborEncodable {
    /**
     * @param mapper The [ObjectMapper] instance that creates the returned [ByteArray].
     * For CBOR encoding, it's expected that the [ObjectMapper] is aware of and utilizes the
     * relevant `[De|S]erializer`s. Defaults to [CborMapper.default], which respects classes
     * annotated with [com.fasterxml.jackson.databind.annotation.JsonSerialize] and
     * [com.fasterxml.jackson.databind.annotation.JsonDeserialize].
     *
     * @return A CBOR-encoded representation of this object.
     */
    fun toCbor(mapper: ObjectMapper = CborMapper.default): ByteArray =
        mapper.writeValueAsBytes(this)
}
