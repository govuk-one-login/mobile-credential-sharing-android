package uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth

import java.security.MessageDigest
import java.security.cert.Certificate
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.cryptoService.cryptography.Constants
import uk.gov.onelogin.sharing.cryptoService.holder.ES256_ALGORITHM

/**
 * Creates the protected headers for the COSE_Sign1 structure. This is defined as:
 *
 * ```
 * { 1: -7, 34: [ -16, sha256(leafCertificate) ] }
 * ```
 */
class CoseSign1ProtectedHeaders(
    private val logger: Logger,
) : ProtectedHeaderGenerator {
    override fun generateProtectedHeaders(leafCertificate: Certificate): Map<UInt, Any> =
        mapOf(
            ECReaderAuthProvider.PROTECTED_HEADER_ALGORITHM to ES256_ALGORITHM, // alg = -7 ECDSA 256
            ECReaderAuthProvider.PROTECTED_HEADER_X5T to arrayOf(
                ECReaderAuthProvider.PROTECTED_HEADER_VALUE_SHA256,
                MessageDigest
                    .getInstance(Constants.HASH_ALGORITHM_SHA256)
                    .digest(leafCertificate.encoded),
            ),
        ).also {
            logger.debug(
                logTag,
                "Generated protected headers for COSE_Sign1 structure"
            )
        }
}
