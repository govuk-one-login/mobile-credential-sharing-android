package uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth

import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import java.io.ByteArrayOutputStream
import java.security.cert.Certificate
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.logger.logTag

/**
 * [SigStructureGenerator] base implementation that returns a CBOR-encoded `COSE_Sign1` data
 * structure.
 *
 * This is defined as:
 *
 * ```
 * [
 *   "Signature1",
 *   protectedHeaderBytes,
 *   h'', // empty byte string
 *   ReaderAuthenticationBytes
 * ]
 * ```
 */
class CoseSigStructureGenerator(
    private val logger: Logger,
    private val protectedHeaderGenerator: ProtectedHeaderGenerator,
) : SigStructureGenerator,
    ProtectedHeaderGenerator by protectedHeaderGenerator {
    override fun generateSignatureStructure(
        certificateChain: List<Certificate>,
        readerAuthenticationPayload: ByteArray,
    ): ByteArray = generateSignatureStructureData(
        certificateChain,
        readerAuthenticationPayload
    ).let { sigStructureArray ->
        ByteArrayOutputStream().also { out ->
            CBORFactory().createGenerator(out).use { gen ->
                @Suppress("DEPRECATION")
                gen.writeStartArray(sigStructureArray.size)

                gen.writeString(sigStructureArray[0] as String)
                gen.writeBinary(sigStructureArray[1] as ByteArray)
                // empty strings are written as [CborConstants.BYTE_EMPTY_STRING]
                gen.writeString(sigStructureArray[2] as String)
                gen.writeBinary(sigStructureArray[3] as ByteArray)

                gen.writeEndArray()
            }
        }.toByteArray()
    }.also {
        logger.debug(
            logTag,
            "CBOR-encoded Sig_Structure array: ${it.toHexString()}"
        )
    }

    internal fun generateSignatureStructureData(
        certificateChain: List<Certificate>,
        readerAuthenticationPayload: ByteArray,
    ): Array<out Any> {
        val (_, protectedHeaderBytes) = generateProtectedHeaders(
            certificateChain.first()
        )

        return arrayOf(
            "Signature1",
            protectedHeaderBytes,
            "",
            readerAuthenticationPayload
        ).also {
            logger.debug(
                logTag,
                "Generated Sig_Structure with protected headers and reader auth bytes"
            )
        }

    }
}