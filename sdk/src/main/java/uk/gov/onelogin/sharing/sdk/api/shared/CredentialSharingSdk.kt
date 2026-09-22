package uk.gov.onelogin.sharing.sdk.api.shared

import java.security.cert.X509Certificate
import uk.gov.onelogin.sharing.orchestration.CredentialProvider
import uk.gov.onelogin.sharing.sdk.api.presenter.CredentialPresenter
import uk.gov.onelogin.sharing.sdk.api.presenter.PresentCredentialSdk
import uk.gov.onelogin.sharing.sdk.api.verifier.VerifyCredentialSdk

interface CredentialSharingSdk {
    val appGraph: CredentialSharingAppGraph

    @Suppress("DEPRECATION")
    @Deprecated(
        message = "Use createCredentialPresenter instead",
        level = DeprecationLevel.WARNING,
        replaceWith = ReplaceWith(
            "createCredentialPresenter(credentialProvider, trustedReaderCertificates)"
        )
    )
    val presentCredentialSdk: PresentCredentialSdk

    val verifyCredentialSdk: VerifyCredentialSdk

    fun createCredentialPresenter(
        credentialProvider: CredentialProvider,
        trustedReaderCertificates: List<X509Certificate> = emptyList()
    ): CredentialPresenter
}
