package uk.gov.onelogin.sharing.testapp

import uk.gov.onelogin.sharing.di.api.presenter.PresenterCredentialSdk
import uk.gov.onelogin.sharing.di.api.shared.CredentialSharingAppGraph
import uk.gov.onelogin.sharing.di.api.shared.CredentialSharingSdk
import uk.gov.onelogin.sharing.di.api.verifier.VerifierCredentialSdk

class FakeCredentialSharingSdk(
    override val appGraph: CredentialSharingAppGraph,
    override val presenterCredentialSdk: PresenterCredentialSdk,
    override val verifierCredentialSdk: VerifierCredentialSdk
) : CredentialSharingSdk
