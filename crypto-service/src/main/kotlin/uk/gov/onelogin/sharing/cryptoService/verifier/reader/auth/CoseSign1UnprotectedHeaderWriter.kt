package uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth

import com.fasterxml.jackson.core.JsonGenerator
import uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth.UnprotectedHeaderGenerator.Companion.UNPROTECTED_HEADER_X5_CHAIN

private const val UNPROTECTED_HEADER_MAP_SIZE = 1

/**
 * Writes the single-entry COSE_Sign1 unprotected header map `{ 33: x5chain }` structurally into
 * [gen], delegating the `x5chain` value encoding to [writeX5Chain].
 */
internal fun writeUnprotectedHeaderMap(gen: JsonGenerator, chain: List<ByteArray>) {
    gen.writeStartObject(UNPROTECTED_HEADER_MAP_SIZE)
    gen.writeFieldId(UNPROTECTED_HEADER_X5_CHAIN)
    writeX5Chain(gen, chain)
    gen.writeEndObject()
}

/**
 * Writes the `x5chain` value into [gen]. A single certificate is encoded as a
 * bare CBOR byte string, while two or more certificates are encoded as an array of byte strings.
 */
internal fun writeX5Chain(gen: JsonGenerator, chain: List<ByteArray>) {
    require(chain.isNotEmpty()) { "x5chain must contain at least one certificate" }
    if (chain.size == 1) {
        gen.writeBinary(chain.single())
    } else {
        @Suppress("DEPRECATION")
        gen.writeStartArray(chain.size)
        chain.forEach(gen::writeBinary)
        gen.writeEndArray()
    }
}
