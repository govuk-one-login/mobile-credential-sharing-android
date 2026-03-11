package uk.gov.onelogin.sharing

import uk.gov.onelogin.orchestration.CredentialProviderNewImpl
import uk.gov.onelogin.orchestration.Orchestrator
import uk.gov.onelogin.sharing.di.CredentialSharingAppGraph

interface PresenterCredentialSdk {
    fun presenter(credentialProvider: CredentialProviderNewImpl): CredentialPresenterNew
}

interface CredentialPresenterNew {
    val appGraph: CredentialSharingAppGraph

    val orchestrator: Orchestrator.Holder
}

class CredentialPresenterNewImpl(
    @Suppress("UnusedPrivateProperty")
    private val credentialProvider: CredentialProviderNewImpl,
    override val orchestrator: Orchestrator.Holder,
    override val appGraph: CredentialSharingAppGraph
) : CredentialPresenterNew
