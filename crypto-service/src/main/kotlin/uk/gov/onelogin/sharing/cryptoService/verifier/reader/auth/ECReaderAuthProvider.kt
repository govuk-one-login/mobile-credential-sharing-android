package uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import java.io.ByteArrayOutputStream
import java.security.InvalidKeyException
import java.security.SignatureException
import java.security.cert.X509Certificate
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.models.mdoc.exceptions.UnrecoverableError

private const val COSE_SIGN1_ARRAY_SIZE = 4

/**
 * Sample [ReaderAuthCredentialProvider] implementation that handles creating `COSE_Sign1`
 * [ByteArray] instances.
 *
 * This is achieved via interface delegation to the proceeding properties:
 * - `sigStructureGenerator`
 * - `protectedHeaderGenerator`
 * - `unprotectedHeaderGenerator`
 *
 * The `COSE_Sign1` structure is defined as:
 * ```
 * [
 *   protectedHeaderBytes,
 *   unprotectedHeaderMap,
 *   null,
 *   signatureBytes
 * ]
 * ```
 *
 * @property certificateChain The X509 certificate chain to use. The [List] begins with the
 * uppermost certificate, with the last element being the relevant leaf certificate.
 * @property logger The GOV.UK [Logger] to send status updates to.
 * @property sigStructureGenerator The [SigStructureGenerator] implementation that generates part of
 * the `COSE_Sign1` structure.
 * @property protectedHeaderGenerator The [ProtectedHeaderGenerator] implementation that generates
 * part of the `COSE_Sign1` structure.
 * @property unprotectedHeaderGenerator The [UnprotectedHeaderGenerator] implementation that
 * generates part of the `COSE_Sign1` structure.
 */
class ECReaderAuthProvider(
    private val logger: Logger,
    private val certificateChain: List<X509Certificate>,
    private val sigStructureGenerator: SigStructureGenerator,
    private val protectedHeaderGenerator: ProtectedHeaderGenerator,
    private val unprotectedHeaderGenerator: UnprotectedHeaderGenerator
) : ReaderAuthCredentialProvider,
    ProtectedHeaderGenerator by protectedHeaderGenerator,
    SigStructureGenerator by sigStructureGenerator,
    UnprotectedHeaderGenerator by unprotectedHeaderGenerator {

    override fun sign(readerAuthenticationPayload: ByteArray): ByteArray = try {
        val (_, protectedHeaderBytes) = generateProtectedHeaders(
            leafCertificate = certificateChain.first()
        )
        val (unprotectedHeaderMap, _) = generateUnprotectedHeaders(
            certificateChain = certificateChain
        )

        val signatureBytes = generateSignatureStructure(
            certificateChain = certificateChain,
            readerAuthenticationPayload = readerAuthenticationPayload
        )

        assembleCoseSign1(
            protectedHeaderBytes = protectedHeaderBytes,
            unprotectedHeaderMap = unprotectedHeaderMap,
            signatureBytes = signatureBytes
        ).also {
            logger.debug(
                logTag,
                "Created COSE_Sign1 structure"
            )
        }
    } catch (invalidKey: InvalidKeyException) {
        UnrecoverableError(
            message = "Couldn't initialise signing with the provided Private Key.",
            cause = invalidKey
        ).let {
            logger.error(logTag, "${it.message}", it)
            throw it
        }
    } catch (signature: SignatureException) {
        UnrecoverableError(
            message = "Couldn't create signature from provided reader authentication bytes.",
            cause = signature
        ).let {
            logger.error(logTag, "${it.message}", it)
            throw it
        }
    }

    /**
     * Assembles the final untagged four-element `COSE_Sign1` ReaderAuth array:
     * ```
     * [
     *   protectedHeaderBytes,   // CBOR byte string (bstr)
     *   unprotectedHeaderMap,   // CBOR map { 33: [leafDER, intermediateDER] }
     *   null,                   // detached payload, explicit CBOR null
     *   signatureBytes          // 64-byte raw COSE R || S signature (bstr)
     * ]
     * ```
     *
     * The unprotected headers are written structurally as a genuine CBOR map (not wrapped in a
     * byte string). The payload element is written as an explicit CBOR `null` (major type 7) to
     * represent the detached payload. The array uses a definite length of four and is left
     * untagged.
     */
    private fun assembleCoseSign1(
        protectedHeaderBytes: ByteArray,
        unprotectedHeaderMap: Map<Long, Any>,
        signatureBytes: ByteArray
    ): ByteArray = ByteArrayOutputStream().also { output ->
        CBORFactory().createGenerator(output).use { gen ->
            gen.writeStartArray(null, COSE_SIGN1_ARRAY_SIZE)

            gen.writeBinary(protectedHeaderBytes)

            writeUnprotectedHeaderMap(gen, unprotectedHeaderMap)

            gen.writeNull()

            gen.writeBinary(signatureBytes)

            gen.writeEndArray()
        }
    }.toByteArray()

    /**
     * Writes the unprotected header map { 33: [leafDER, intermediateDER] } structurally into the
     * supplied [gen]. The single entry keyed by [UnprotectedHeaderGenerator.UNPROTECTED_HEADER_X5_CHAIN]
     * holds the ordered, leaf-first certificate DER chain.
     */
    private fun writeUnprotectedHeaderMap(
        gen: JsonGenerator,
        unprotectedHeaderMap: Map<Long, Any>
    ) {
        gen.writeStartObject(unprotectedHeaderMap.size)

        val x5Chain = (
            unprotectedHeaderMap[UnprotectedHeaderGenerator.UNPROTECTED_HEADER_X5_CHAIN]
                as Array<*>
            ).map { it as ByteArray }

        gen.writeFieldId(UnprotectedHeaderGenerator.UNPROTECTED_HEADER_X5_CHAIN)
        @Suppress("DEPRECATION")
        gen.writeStartArray(x5Chain.size)
        x5Chain.forEach(gen::writeBinary)
        gen.writeEndArray()

        gen.writeEndObject()
    }
}
