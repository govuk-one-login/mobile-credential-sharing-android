package uk.gov.onelogin.sharing.di.api.presenter

import uk.gov.onelogin.CredentialProvider

fun interface PresentCredentialSdk {
    fun presenter(credentialProvider: CredentialProvider): CredentialPresenter
}
