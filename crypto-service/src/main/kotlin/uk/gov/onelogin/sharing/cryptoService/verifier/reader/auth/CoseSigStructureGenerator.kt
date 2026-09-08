package uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth

import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import java.io.ByteArrayOutputStream
import java.security.cert.Certificate
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.logger.logTag

private const val INDEX_CONTEXT = 0
private const val INDEX_PROTECTED_HEADER = 1
private const val INDEX_EXTERNAL_AAD = 2
private const val INDEX_PAYLOAD = 3

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
    private val protectedHeaderGenerator: ProtectedHeaderGenerator
) : SigStructureGenerator,
    ProtectedHeaderGenerator by protectedHeaderGenerator {
    override fun generateSignatureStructure(
        certificateChain: List<Certificate>,
        readerAuthenticationPayload: ByteArray
    ): ByteArray = generateSignatureStructureData(
        certificateChain,
        readerAuthenticationPayload
    ).let { sigStructureArray ->
        ByteArrayOutputStream().also { out ->
            CBORFactory().createGenerator(out).use { gen ->
                gen.writeStartArray(null, sigStructureArray.size)

                gen.writeString(sigStructureArray[INDEX_CONTEXT] as String)
                gen.writeBinary(sigStructureArray[INDEX_PROTECTED_HEADER] as ByteArray)
                // external_aad is an empty CBOR byte string (h''), per COSE Sig_structure.
                gen.writeBinary(sigStructureArray[INDEX_EXTERNAL_AAD] as ByteArray)
                gen.writeBinary(sigStructureArray[INDEX_PAYLOAD] as ByteArray)

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
        readerAuthenticationPayload: ByteArray
    ): Array<out Any> {
        val (_, protectedHeaderBytes) = generateProtectedHeaders(
            certificateChain.first()
        )

        return arrayOf(
            "Signature1",
            protectedHeaderBytes,
            ByteArray(0),
            readerAuthenticationPayload
        ).also {
            logger.debug(
                logTag,
                "Generated Sig_Structure with protected headers and reader auth bytes"
            )
        }
    }
}
