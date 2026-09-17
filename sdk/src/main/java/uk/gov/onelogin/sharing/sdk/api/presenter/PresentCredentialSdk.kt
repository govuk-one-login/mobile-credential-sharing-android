package uk.gov.onelogin.sharing.sdk.api.presenter

import uk.gov.onelogin.sharing.orchestration.CredentialProvider

@Deprecated(
    message = "Use CredentialSharingSdk.createCredentialPresenter instead",
    level = DeprecationLevel.WARNING,
    replaceWith = ReplaceWith("createCredentialPresenter(credentialProvider)")
)
fun interface PresentCredentialSdk {
    fun presenter(credentialProvider: CredentialProvider): CredentialPresenter
}
