package uk.gov.onelogin.sharing.testapp.verifier.auth.reader

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import uk.gov.onelogin.sharing.testapp.credential.attribute.select.ReaderAuthOption
import uk.gov.onelogin.sharing.testapp.credential.attribute.select.ReaderRootOption

/**
 * Holds the currently selected [ReaderRootOption] for the Holder test app and resolves it into
 * the trusted ReaderAuth root [X509Certificate]s used when constructing the SDK's presenter.
 */
@Singleton
class ReaderRootCertificateProvider @Inject constructor(
    @ApplicationContext
    private val context: Context
) {
    private val certificateFactory: CertificateFactory =
        CertificateFactory.getInstance("X.509")

    private val _readerRootOption: MutableStateFlow<ReaderRootOption> = MutableStateFlow(
        ReaderRootOption.SHARING_TEST_APP_MOCK
    )

    val readerRootOption: Flow<ReaderRootOption> = _readerRootOption

    fun update(option: ReaderRootOption) {
        _readerRootOption.value = option
    }

    /**
     * Reads and parses the trusted reader root certificates based on the selected [ReaderRootOption].
     */
    fun trustedReaderCertificates(): List<X509Certificate> = when (_readerRootOption.value) {
        ReaderRootOption.ALL -> allTrustedRootCertificates()

        ReaderRootOption.SHARING_TEST_APP_MOCK ->
            listOfNotNull(rootCertificateFor(ReaderAuthOption.VALID))

        ReaderRootOption.DVS_DEV ->
            listOfNotNull(rootCertificateFor(ReaderAuthOption.DVS_DEV))

        ReaderRootOption.DVS_INTEGRATION ->
            listOfNotNull(rootCertificateFor(ReaderAuthOption.DVS_INTEGRATION))
    }

    fun allTrustedRootCertificates(): List<X509Certificate> =
        ReaderAuthOption.entries.mapNotNull { rootCertificateFor(it) }.distinct()

    fun rootCertificateFor(option: ReaderAuthOption): X509Certificate? = try {
        val certs = option.certificateChain.flatMap { asset ->
            context.assets.open(asset).use { stream ->
                certificateFactory.generateCertificates(stream)
                    .filterIsInstance<X509Certificate>()
            }
        }
        certs.firstOrNull(::isSelfSigned) ?: certs.lastOrNull()
    } catch (_: Exception) {
        null
    }

    private fun isSelfSigned(cert: X509Certificate): Boolean = try {
        cert.verify(cert.publicKey)
        true
    } catch (_: java.security.GeneralSecurityException) {
        false
    }
}
