package uk.gov.onelogin.sharing.testapp.verifier.auth.reader

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.InputStreamReader
import java.security.KeyFactory
import java.security.Signature
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.interfaces.ECPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.encoding.Base64
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth.CoseSigStructureGenerator
import uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth.CoseSign1ProtectedHeaders
import uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth.CoseSign1UnprotectedHeaderGenerator
import uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth.ECReaderAuthProvider
import uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth.ReaderAuthCredentialProvider
import uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth.SigningSignatureStructure
import uk.gov.onelogin.sharing.testapp.credential.SIGNING_ALGORITHM
import uk.gov.onelogin.sharing.testapp.credential.attribute.select.ReaderAuthOption

/**
 * Sample implementation of [ReaderAuthCredentialProvider.Factory].
 *
 * Internally manages the currently selected [ReaderAuthOption] that points to the reader
 * authentication's private key to use within the created [ReaderAuthCredentialProvider].
 */
@Singleton
class TestAppReaderAuthCredentialProviderFactory(
    @ApplicationContext
    private val context: Context,
    private val logger: Logger,
    initialState: ReaderAuthOption,
    private val keyFactory: KeyFactory,
    private val certificateFactory: CertificateFactory
) : ReaderAuthCredentialProvider.Factory {

    @Inject
    constructor(
        @ApplicationContext
        context: Context,
        logger: Logger
    ) : this(
        context = context,
        logger = logger,
        initialState = ReaderAuthOption.VALID,
        keyFactory = KeyFactory.getInstance("EC"),
        certificateFactory = CertificateFactory.getInstance("X.509")
    )

    private val _readerAuthOption: MutableStateFlow<ReaderAuthOption> = MutableStateFlow(
        initialState
    )

    val readerAuthOption: Flow<ReaderAuthOption> = _readerAuthOption

    /**
     * Builds a [ReaderAuthCredentialProvider] for the currently selected [ReaderAuthOption].
     *
     * This reads and parses the reader authentication key and certificate chain assets.
     * It is a blocking operation and callers are expected to invoke it off the main thread.
     */
    override fun create(): ReaderAuthCredentialProvider {
        val option = _readerAuthOption.value
        val privateKeyChain = processPrivateKeyAssetChain(option.privateKeyChain.asSequence())
        val certificateChain = processCertificateAssetChain(option.certificateChain.asSequence())

        return ECReaderAuthProvider(
            // x5chain must contain the leaf and intermediate(s) only, with the root excluded
            // (per ISO 18013-5 ReaderAuth). The asset chain is leaf-first and ends with the root,
            // so drop the last element.
            certificateChain = certificateChain.dropLast(1),
            logger = logger,
            protectedHeaderGenerator = CoseSign1ProtectedHeaders(logger),
            unprotectedHeaderGenerator = CoseSign1UnprotectedHeaderGenerator(logger),
            sigStructureGenerator = SigningSignatureStructure(
                logger = logger,
                signature = Signature.getInstance(SIGNING_ALGORITHM),
                privateKey = privateKeyChain.first(),
                decorated = CoseSigStructureGenerator(
                    logger = logger,
                    protectedHeaderGenerator = CoseSign1ProtectedHeaders(logger = logger)
                )
            )
        )
    }

    fun update(option: ReaderAuthOption) {
        _readerAuthOption.value = option
    }

    private fun processPrivateKeyAssetChain(chain: Sequence<String>): List<ECPrivateKey> = chain
        .map(context.assets::open)
        .map(::InputStreamReader)
        .map(InputStreamReader::readText)
        .map(CharSequence::lines)
        .map { privateKeyLines ->
            privateKeyLines.filterNot { it.startsWith("-----") }
        }
        .map { it.joinToString("") }
        .map(Base64::decode)
        .map(::PKCS8EncodedKeySpec)
        .map(keyFactory::generatePrivate)
        .map { it as ECPrivateKey }
        .toList()

    private fun processCertificateAssetChain(chain: Sequence<String>): List<X509Certificate> = chain
        .map(context.assets::open)
        .map(certificateFactory::generateCertificate)
        .map { it as X509Certificate }
        .toList()
}
