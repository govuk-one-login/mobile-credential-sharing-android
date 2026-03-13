package uk.gov.onelogin.sharing.testapp

import uk.gov.onelogin.sharing.di.api.presenter.PresentCredentialSdk
import uk.gov.onelogin.sharing.di.api.shared.CredentialSharingAppGraph
import uk.gov.onelogin.sharing.di.api.shared.CredentialSharingSdk
import uk.gov.onelogin.sharing.di.api.verifier.VerifyCredentialSdk

class FakeCredentialSharingSdk(
    override val appGraph: CredentialSharingAppGraph,
    override val presentCredentialSdk: PresentCredentialSdk,
    override val verifyCredentialSdk: VerifyCredentialSdk
) : CredentialSharingSdk
