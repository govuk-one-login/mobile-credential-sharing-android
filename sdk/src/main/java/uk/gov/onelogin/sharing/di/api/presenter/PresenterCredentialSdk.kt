package uk.gov.onelogin.sharing.di.api.presenter

import uk.gov.onelogin.CredentialProvider

fun interface PresenterCredentialSdk {
    fun presenter(credentialProvider: CredentialProvider): CredentialPresenter
}
