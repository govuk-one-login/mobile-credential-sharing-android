package uk.gov.onelogin.sharing.sdk.api.shared

import uk.gov.onelogin.sharing.sdk.api.presenter.PresentCredentialSdk
import uk.gov.onelogin.sharing.sdk.api.verifier.VerifyCredentialSdk

interface CredentialSharingSdk {
    val appGraph: CredentialSharingAppGraph
    val presentCredentialSdk: PresentCredentialSdk
    val verifyCredentialSdk: VerifyCredentialSdk
}
