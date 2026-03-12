package uk.gov.onelogin.sharing.di.api.presenter

fun interface PresenterCredentialSdk {
    fun presenter(credentialProvider: CredentialProvider): CredentialPresenter
}
