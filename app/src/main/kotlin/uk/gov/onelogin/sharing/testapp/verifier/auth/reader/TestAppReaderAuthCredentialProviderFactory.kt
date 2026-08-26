package uk.gov.onelogin.sharing.testapp.verifier.auth.reader

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.InputStreamReader
import java.security.KeyFactory
import java.security.interfaces.ECPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.encoding.Base64
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import uk.gov.onelogin.sharing.orchestration.verifier.auth.reader.ECReaderAuthProvider
import uk.gov.onelogin.sharing.orchestration.verifier.auth.reader.ReaderAuthCredentialProvider
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
    initialState: ReaderAuthOption
) : ReaderAuthCredentialProvider.Factory {

    @Inject constructor(
        @ApplicationContext
        context: Context
    ) : this(
        context = context,
        initialState = ReaderAuthOption.VALID
    )

    private val _readerAuthOption: MutableStateFlow<ReaderAuthOption> = MutableStateFlow(
        initialState
    )
    val readerAuthOption: Flow<ReaderAuthOption> = _readerAuthOption

    override fun create(): ReaderAuthCredentialProvider {
        val privateKeySpec = context.assets.open(
            _readerAuthOption.value.leafCertificatePrivateKeyFileName
        ).let(::InputStreamReader)
            .readText()
            .lines()
            .filterNot { it.startsWith("-----") }
            .joinToString("")
            .also { require(it.isNotBlank()) { "PEM file has no key content" } }
            .let(Base64::decode)
            .let(::PKCS8EncodedKeySpec)

        return ECReaderAuthProvider(
            privateKey = KeyFactory.getInstance(
                "EC"
            ).generatePrivate(privateKeySpec) as ECPrivateKey,
            signingAlgorithm = SIGNING_ALGORITHM
        )
    }

    fun update(option: ReaderAuthOption) {
        _readerAuthOption.value = option
    }
}
