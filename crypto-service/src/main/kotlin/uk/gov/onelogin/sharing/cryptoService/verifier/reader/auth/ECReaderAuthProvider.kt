package uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth

import java.security.InvalidKeyException
import java.security.MessageDigest
import java.security.Signature
import java.security.SignatureException
import java.security.cert.X509Certificate
import java.security.interfaces.ECPrivateKey
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.cryptoService.cryptography.Constants.HASH_ALGORITHM_SHA256
import uk.gov.onelogin.sharing.cryptoService.holder.ES256_ALGORITHM
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
    private val signature: Signature
) : ReaderAuthCredentialProvider {

    override fun sign(readerAuthenticationPayload: ByteArray): ByteArray = try {
        signature.run {
            initSign(privateKeyChain.last())
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

    /**
     * Creates the protected headers for the COSE_Sign1 structure. This is defined as:
     *
     * ```
     * { 1: -7, 34: [ -16, sha256(leafCertificate) ] }
     * ```
     */
    internal fun generateProtectedHeaders(): Map<UInt, Any> {
        return mapOf(
            PROTECTED_HEADER_ALGORITHM to ES256_ALGORITHM, // alg = -7 ECDSA 256
            PROTECTED_HEADER_X5T to arrayOf(
                PROTECTED_HEADER_VALUE_SHA256,
                MessageDigest
                    .getInstance(HASH_ALGORITHM_SHA256)
                    .digest(certificateChain.last().encoded),
                ),
        ).also {
            logger.debug(
                logTag,
                "Generated protected headers for COSE_Sign1 structure"
            )
        }
    }

    companion object {
        internal const val PROTECTED_HEADER_X5T = 34U
        internal const val PROTECTED_HEADER_ALGORITHM = 1U
        internal const val PROTECTED_HEADER_VALUE_SHA256 = -16
    }
}