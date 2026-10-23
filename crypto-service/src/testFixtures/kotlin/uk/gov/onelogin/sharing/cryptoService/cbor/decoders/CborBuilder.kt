package uk.gov.onelogin.sharing.cryptoService.cbor.decoders

import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator
import java.io.ByteArrayOutputStream

/**
 * Builds a CBOR byte array using a [CBORGenerator], for constructing test inputs.
 */
fun cbor(build: (CBORGenerator) -> Unit): ByteArray {
    val out = ByteArrayOutputStream()
    CBORFactory().createGenerator(out).use { gen -> build(gen as CBORGenerator) }
    return out.toByteArray()
}
