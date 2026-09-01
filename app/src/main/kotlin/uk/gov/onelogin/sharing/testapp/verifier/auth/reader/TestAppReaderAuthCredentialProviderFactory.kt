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
import kotlin.coroutines.CoroutineContext
import kotlin.io.encoding.Base64
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth.ECReaderAuthProvider
import uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth.ReaderAuthCredentialProvider
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
    private val certificateFactory: CertificateFactory,
    private val coroutineContext: CoroutineContext
) : ReaderAuthCredentialProvider.Factory {

    @Inject
    constructor(
        @ApplicationContext
        context: Context,
        logger: Logger,
    ) : this(
        context = context,
        logger = logger,
        initialState = ReaderAuthOption.VALID,
        keyFactory = KeyFactory.getInstance("EC"),
        coroutineContext = Dispatchers.IO,
        certificateFactory = CertificateFactory.getInstance("X.509")
    )

    private val _readerAuthOption: MutableStateFlow<ReaderAuthOption> = MutableStateFlow(
        initialState
    )

    private val privateKeyChain: StateFlow<List<ECPrivateKey>> = _readerAuthOption
        .map { it.privateKeyChain.asSequence() }
        .map { processPrivateKeyAssetChain(it) }
        .stateIn(
            CoroutineScope(coroutineContext),
            SharingStarted.Lazily,
            emptyList()
        )

    private val certificateChain: StateFlow<List<X509Certificate>> = _readerAuthOption
        .map { it.certificateChain.asSequence() }
        .map { processCertificateAssetChain(it) }
        .stateIn(
            CoroutineScope(coroutineContext),
            SharingStarted.Lazily,
            emptyList()
        )

    val readerAuthOption: Flow<ReaderAuthOption> = _readerAuthOption

    override fun create(): ReaderAuthCredentialProvider = ECReaderAuthProvider(
        privateKeyChain = privateKeyChain.value,
        certificateChain = certificateChain.value,
        signature = Signature.getInstance(SIGNING_ALGORITHM),
        logger = logger,
    )

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
