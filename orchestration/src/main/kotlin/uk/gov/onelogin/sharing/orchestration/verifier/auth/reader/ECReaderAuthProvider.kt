package uk.gov.onelogin.sharing.orchestration.verifier.auth.reader

import java.security.InvalidKeyException
import java.security.Signature
import java.security.SignatureException
import java.security.interfaces.ECPrivateKey
import uk.gov.onelogin.sharing.orchestration.exceptions.UnrecoverableError

/**
 * Sample [ReaderAuthCredentialProvider] implementation that signs the provided
 * `readerAuthenticationBytes` with the [privateKey] property.
 *
 * It's assumed that `readerAuthenticationBytes` is already in the shape of a `COSE_Sign1`
 * structure.
 *
 * @property privateKey The EC private key to sign with.
 * @property signature The signing algorithm to be used. e.g. "NONEwithECDSA"
 */
class ECReaderAuthProvider(
    private val privateKey: ECPrivateKey,
    private val signature: Signature,
) : ReaderAuthCredentialProvider {

    override fun sign(
        readerAuthenticationBytes: ByteArray,
    ): ByteArray = try {
        return signature.run {
            initSign(privateKey)
            update(readerAuthenticationBytes)
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
