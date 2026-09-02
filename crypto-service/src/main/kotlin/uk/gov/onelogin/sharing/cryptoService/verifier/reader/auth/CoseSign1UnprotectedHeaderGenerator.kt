package uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth

import javax.security.cert.Certificate
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.logger.logTag

class CoseSign1UnprotectedHeaderGenerator(
    private val logger: Logger
) : UnprotectedHeaderGenerator {
    override fun generateUnprotectedHeaders(
        certificateChain: List<Certificate>,
    ): Map<UInt, Any> = mapOf(
        UNPROTECTED_HEADER_X5_CHAIN to certificateChain.toTypedArray()
    ).also {
        logger.debug(
            logTag,
            "Generated unprotected headers for COSE_Sign1 structure"
        )
    }

    companion object {
        internal const val UNPROTECTED_HEADER_X5_CHAIN = 33U
    }
}