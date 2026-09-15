package uk.gov.onelogin.sharing.testapp.verifier.auth.issuer

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import uk.gov.onelogin.sharing.testapp.credential.attribute.select.IssuerRootOption

/**
 * Holds the currently selected [IssuerRootOption] for the Verifier test app and resolves it into
 * the trusted IssuerAuth root [X509Certificate] used to build the SDK's `VerifierConfig`.
 *
 * The selection is shared application-wide: the selection UI updates it and the verifier journey
 * reads the resolved certificate when constructing the `VerifierConfig`.
 */
@Singleton
class IssuerRootCertificateProvider @Inject constructor(
    @ApplicationContext
    private val context: Context
) {

    private val certificateFactory: CertificateFactory =
        CertificateFactory.getInstance("X.509")

    private val _issuerRootOption: MutableStateFlow<IssuerRootOption> = MutableStateFlow(
        IssuerRootOption.SHARING_TEST_APP_MOCK
    )

    val issuerRootOption: Flow<IssuerRootOption> = _issuerRootOption

    fun update(option: IssuerRootOption) {
        _issuerRootOption.value = option
    }

    /**
     * Reads and parses the currently selected root certificate asset.
     *
     * This performs blocking I/O and should be invoked off the main thread.
     */
    fun trustedRootCertificate(): X509Certificate = context.assets
        .open(_issuerRootOption.value.certificateAsset)
        .use { stream -> certificateFactory.generateCertificate(stream) as X509Certificate }
}
