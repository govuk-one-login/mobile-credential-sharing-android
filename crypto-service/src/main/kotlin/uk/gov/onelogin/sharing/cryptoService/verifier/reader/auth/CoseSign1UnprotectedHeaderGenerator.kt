package uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth

import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import java.io.ByteArrayOutputStream
import java.security.cert.Certificate
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth.UnprotectedHeaderGenerator.Companion.UNPROTECTED_HEADER_X5_CHAIN

/**
 * Creates the unprotected headers for a COSE_Sign1 structure. This is defined as:
 *
 * ```
 * { 33: leafCertificateDER }                              // single certificate: bare bstr
 * { 33: [ leafCertificateDER, intermediateCertificateDER ] } // two or more: array of bstr
 * ```
 */
class CoseSign1UnprotectedHeaderGenerator(private val logger: Logger) : UnprotectedHeaderGenerator {

    private fun generateUnprotectedHeaderData(certificateChain: List<Certificate>): Map<Long, Any> =
        mapOf(
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
        certificateChain: List<Certificate>
    ): Pair<Map<Long, Any>, ByteArray> {
        require(certificateChain.isNotEmpty()) {
            "Certificate chain must contain at least one certificate for the x5chain header"
        }
        return generateUnprotectedHeaderData(certificateChain).let { headers ->
            headers to ByteArrayOutputStream().also { out ->
                CBORFactory().createGenerator(out).use { gen ->
                    val chain = (headers[UNPROTECTED_HEADER_X5_CHAIN] as Array<*>)
                        .map { it as ByteArray }
                    writeUnprotectedHeaderMap(gen, chain)
                }
            }.toByteArray()
        }
    }
}
