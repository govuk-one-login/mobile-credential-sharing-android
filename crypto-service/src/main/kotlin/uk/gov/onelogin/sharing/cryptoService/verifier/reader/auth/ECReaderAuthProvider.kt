package uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth

import java.security.InvalidKeyException
import java.security.SignatureException
import java.security.cert.X509Certificate
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.models.mdoc.exceptions.UnrecoverableError

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
@Suppress("UnusedPrivateProperty")
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
            certificateChain = certificateChain,
        )

        val signatureBytes = generateSignatureStructure(
            certificateChain = certificateChain,
            readerAuthenticationPayload = readerAuthenticationPayload
        )

        // DCMAW-21664: Change to Cose_Sign1 data structure then CBOR encode
        signatureBytes.also {
            logger.debug(
                logTag,
                "Created CBOR-encoded Cose_Sign1 data structure"
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
}