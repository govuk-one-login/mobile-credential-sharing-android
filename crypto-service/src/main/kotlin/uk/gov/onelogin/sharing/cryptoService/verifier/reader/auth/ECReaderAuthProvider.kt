package uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth

import java.security.InvalidKeyException
import java.security.SignatureException
import java.security.cert.X509Certificate
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.models.mdoc.exceptions.UnrecoverableError

/**
 * Sample [ReaderAuthCredentialProvider] implementation that signs the provided
 * `readerAuthenticationBytes` with the [privateKeyChain] property.
 *
 * It's assumed that `readerAuthenticationBytes` is already in the shape of a `COSE_Sign1`
 * structure.
 *
 * @property privateKeyChain The EC private keychain to use. The [List] begins with the uppermost
 * private key, with the last element being the relevant leaf certificate's private key.
 * @property certificateChain The X509 certificate chain to use. The [List] begins with the
 * uppermost certificate, with the last element being the relevant leaf certificate.
 * @property signature The signing algorithm to be used. e.g. "NONEwithECDSA"
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
        val (protectedHeaders, protectedHeaderBytes) = generateProtectedHeaders(
            leafCertificate = certificateChain.first()
        )
        val (unprotectedHeaders, unprotectedHeaderBytes) = generateUnprotectedHeaders(
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