package uk.gov.onelogin.sharing.sdk.api.presenter

import uk.gov.onelogin.sharing.orchestration.CredentialProvider

fun interface PresentCredentialSdk {
    fun presenter(credentialProvider: CredentialProvider): CredentialPresenter
}
