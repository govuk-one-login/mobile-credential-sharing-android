package uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth

import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.security.cert.Certificate
import java.security.interfaces.ECPublicKey
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.cryptoService.cryptography.Constants
import uk.gov.onelogin.sharing.cryptoService.holder.ES256_ALGORITHM
import uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth.ProtectedHeaderGenerator.Companion.PROTECTED_HEADER_ALGORITHM
import uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth.ProtectedHeaderGenerator.Companion.PROTECTED_HEADER_VALUE_SHA256
import uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth.ProtectedHeaderGenerator.Companion.PROTECTED_HEADER_X5T

internal const val ES384_ALGORITHM = -35
private const val P384_KEY_BIT_LENGTH = 384

/**
 * Creates the protected headers for the COSE_Sign1 structure. This is defined as:
 *
 * ```
 * { 1: -7 (or -35 for P-384), 34: [ -16, sha256(leafCertificate) ] }
 * ```
 */
class CoseSign1ProtectedHeaders(private val logger: Logger) : ProtectedHeaderGenerator {
    private fun generateProtectedHeaderData(leafCertificate: Certificate): Map<Long, Any> {
        val ecPublicKey = leafCertificate.publicKey as? ECPublicKey
        val alg = if (ecPublicKey?.params?.order?.bitLength() == P384_KEY_BIT_LENGTH) {
            ES384_ALGORITHM
        } else {
            ES256_ALGORITHM
        }
        return mapOf(
            PROTECTED_HEADER_ALGORITHM to alg,
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
    }

    override fun generateProtectedHeaders(
        leafCertificate: Certificate
    ): Pair<Map<Long, Any>, ByteArray> =
        generateProtectedHeaderData(leafCertificate).let { headers ->
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
