package uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth

import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import java.io.ByteArrayOutputStream
import java.security.cert.Certificate
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth.UnprotectedHeaderGenerator.Companion.UNPROTECTED_HEADER_X5_CHAIN

class CoseSign1UnprotectedHeaderGenerator(
    private val logger: Logger
) : UnprotectedHeaderGenerator {

    internal fun generateUnprotectedHeaderData(
        certificateChain: List<Certificate>
    ): Map<Long, Any> =  mapOf(
        UNPROTECTED_HEADER_X5_CHAIN to certificateChain
            .map(Certificate::getEncoded)
            .toTypedArray()
    ).also {
        logger.debug(
            logTag,
            "Generated unprotected headers for COSE_Sign1 structure"
        )
    }

    override fun generateUnprotectedHeaders(
        certificateChain: List<Certificate>,
    ): Pair<Map<Long, Any>, ByteArray> = generateUnprotectedHeaderData(certificateChain).let { headers ->
        headers to ByteArrayOutputStream().also { out ->
            CBORFactory().createGenerator(out).use { gen ->
                gen.writeStartObject(headers.size)

                val chain = (headers[UNPROTECTED_HEADER_X5_CHAIN] as Array<*>)
                    .map { it as ByteArray }


                gen.writeFieldId(UNPROTECTED_HEADER_X5_CHAIN)
                @Suppress("DEPRECATION")
                gen.writeStartArray(chain.size)
                repeat(chain.size) { index ->
                    gen.writeBinary(chain[index])
                }
                gen.writeEndArray()
                gen.writeEndObject()
            }
        }.toByteArray()
    }
}
