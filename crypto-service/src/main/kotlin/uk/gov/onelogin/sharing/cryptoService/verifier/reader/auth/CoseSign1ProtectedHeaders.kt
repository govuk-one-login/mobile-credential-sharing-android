package uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth

import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.security.cert.Certificate
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.cryptoService.cryptography.Constants
import uk.gov.onelogin.sharing.cryptoService.holder.ES256_ALGORITHM
import uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth.ProtectedHeaderGenerator.Companion.PROTECTED_HEADER_ALGORITHM
import uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth.ProtectedHeaderGenerator.Companion.PROTECTED_HEADER_VALUE_SHA256
import uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth.ProtectedHeaderGenerator.Companion.PROTECTED_HEADER_X5T

/**
 * Creates the protected headers for the COSE_Sign1 structure. This is defined as:
 *
 * ```
 * { 1: -7, 34: [ -16, sha256(leafCertificate) ] }
 * ```
 */
class CoseSign1ProtectedHeaders(private val logger: Logger) : ProtectedHeaderGenerator {
    private fun generateUnprotectedHeaderData(leafCertificate: Certificate): Map<Long, Any> = mapOf(
        PROTECTED_HEADER_ALGORITHM to ES256_ALGORITHM, // alg = -7 ECDSA 256
        PROTECTED_HEADER_X5T to arrayOf(
            PROTECTED_HEADER_VALUE_SHA256,
            MessageDigest
                .getInstance(Constants.HASH_ALGORITHM_SHA256)
                .digest(leafCertificate.encoded)
        )
    ).also {
        logger.debug(
            logTag,
            "Generated protected headers for COSE_Sign1 structure"
        )
    }

    override fun generateProtectedHeaders(
        leafCertificate: Certificate
    ): Pair<Map<Long, Any>, ByteArray> =
        generateUnprotectedHeaderData(leafCertificate).let { headers ->
            headers to ByteArrayOutputStream().also { out ->
                CBORFactory().createGenerator(out).use { gen ->
                    gen.writeStartObject(headers.size)

                    val algorithm = headers[PROTECTED_HEADER_ALGORITHM] as Int
                    gen.writeFieldId(PROTECTED_HEADER_ALGORITHM)
                    gen.writeNumber(algorithm)

                    val x5tArray = headers[PROTECTED_HEADER_X5T] as Array<*>
                    gen.writeFieldId(PROTECTED_HEADER_X5T)
                    @Suppress("DEPRECATION")
                    gen.writeStartArray(x5tArray.size)
                    gen.writeNumber(x5tArray[0] as Int)
                    gen.writeBinary(x5tArray[1] as ByteArray)
                    gen.writeEndArray()

                    gen.writeEndObject()
                }
            }.toByteArray()
        }
}
