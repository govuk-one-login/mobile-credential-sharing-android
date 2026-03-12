package uk.gov.onelogin.sharing.di.api.shared

import uk.gov.onelogin.sharing.di.api.presenter.PresenterCredentialSdk
import uk.gov.onelogin.sharing.di.api.verifier.VerifierCredentialSdk

interface CredentialSharingSdk {
    val appGraph: CredentialSharingAppGraph
    val presenterCredentialSdk: PresenterCredentialSdk
    val verifierCredentialSdk: VerifierCredentialSdk
}
