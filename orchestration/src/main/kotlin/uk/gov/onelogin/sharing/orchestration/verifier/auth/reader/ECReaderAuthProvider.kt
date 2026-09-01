package uk.gov.onelogin.sharing.orchestration.verifier.auth.reader

import java.security.InvalidKeyException
import java.security.Signature
import java.security.SignatureException
import java.security.cert.X509Certificate
import java.security.interfaces.ECPrivateKey
import uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth.ReaderAuthCredentialProvider
import uk.gov.onelogin.sharing.models.mdoc.exceptions.UnrecoverableError

/**
 * Sample [uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth.ReaderAuthCredentialProvider] implementation that signs the provided
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
    private val privateKeyChain: List<ECPrivateKey>,
    private val certificateChain: List<X509Certificate>,
    private val signature: Signature
) : ReaderAuthCredentialProvider {

    override fun sign(readerAuthenticationPayload: ByteArray): ByteArray = try {
        signature.run {
            initSign(privateKeyChain.last())
            update(readerAuthenticationPayload)
            sign()
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
