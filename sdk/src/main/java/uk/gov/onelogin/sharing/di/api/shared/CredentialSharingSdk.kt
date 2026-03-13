package uk.gov.onelogin.sharing.di.api.shared

import uk.gov.onelogin.sharing.di.api.presenter.PresentCredentialSdk
import uk.gov.onelogin.sharing.di.api.verifier.VerifyCredentialSdk

interface CredentialSharingSdk {
    val appGraph: CredentialSharingAppGraph
    val presentCredentialSdk: PresentCredentialSdk
    val verifyCredentialSdk: VerifyCredentialSdk
}
