package uk.gov.onelogin.sharing.testapp

import java.security.cert.X509Certificate
import uk.gov.onelogin.sharing.orchestration.CredentialProvider
import uk.gov.onelogin.sharing.sdk.api.presenter.CredentialPresenter
import uk.gov.onelogin.sharing.sdk.api.presenter.PresentCredentialSdk
import uk.gov.onelogin.sharing.sdk.api.shared.CredentialSharingAppGraph
import uk.gov.onelogin.sharing.sdk.api.shared.CredentialSharingSdk
import uk.gov.onelogin.sharing.sdk.api.verifier.VerifyCredentialSdk
import uk.gov.onelogin.sharing.verification.cose.CoseVerificationFailure

class FakeCredentialSharingSdk(
    override val appGraph: CredentialSharingAppGraph,
    @Suppress("DEPRECATION")
    @get:Deprecated("Use createCredentialPresenter instead")
    override val presentCredentialSdk: PresentCredentialSdk,
    override val verifyCredentialSdk: VerifyCredentialSdk,
    private val credentialPresenter: CredentialPresenter? = null
) : CredentialSharingSdk {
    override fun createCredentialPresenter(
        credentialProvider: CredentialProvider,
        trustedReaderCertificates: List<X509Certificate>
    ): CredentialPresenter {
        if (trustedReaderCertificates.isEmpty()) {
            throw CoseVerificationFailure.UntrustedCertificate
        }
        return credentialPresenter
            ?:
            @Suppress("DEPRECATION")
            presentCredentialSdk.presenter(credentialProvider)
    }
}
