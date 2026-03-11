package uk.gov.onelogin.sharing.sdk

import uk.gov.onelogin.orchestration.Orchestrator
import uk.gov.onelogin.sharing.CredentialPresenterNew
import uk.gov.onelogin.sharing.CredentialVerifierNew
import uk.gov.onelogin.sharing.di.CredentialSharingAppGraph

class FakeCredentialVerifierNew(
    override val appGraph: CredentialSharingAppGraph,
    override val orchestrator: Orchestrator.Verifier
) : CredentialVerifierNew {


}