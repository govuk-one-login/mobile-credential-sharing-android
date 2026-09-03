package uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth

import java.security.InvalidKeyException
import java.security.Signature
import java.security.SignatureException
import java.security.cert.X509Certificate
import java.security.interfaces.ECPrivateKey
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
    private val privateKeyChain: List<ECPrivateKey>,
    private val certificateChain: List<X509Certificate>,
    private val signature: Signature,
    private val protectedHeaderGenerator: ProtectedHeaderGenerator,
    private val unprotectedHeaderGenerator: UnprotectedHeaderGenerator,
) : ReaderAuthCredentialProvider,
    ProtectedHeaderGenerator by protectedHeaderGenerator,
    UnprotectedHeaderGenerator by unprotectedHeaderGenerator {

    /**
     * 1. protectedHeaderBytes encoded
     * 2. put into Sig_structure
     * 3. Sig_structure encoded
     * 4. Sig_structure signed
     * put into COSE_Sign1
     * 5. COSE_Sign1 encoded
     */
    override fun sign(readerAuthenticationPayload: ByteArray): ByteArray = try {
        signature.run {
            initSign(privateKeyChain.first())
            update(readerAuthenticationPayload)
            sign().also {
                logger.debug(
                    logTag,
                    "Created COSE_Sign1 structure"
                )
            }
        }
    } catch (invalidKey: InvalidKeyException) {
        throw UnrecoverableError(
            message = "Couldn't initialise signing with the provided Private Key.",
            cause = invalidKey
        )
    } catch (signature: SignatureException) {
        throw UnrecoverableError(
            message = "Couldn't create signature from provided reader authentication bytes.",
            cause = signature
        )
    }
}