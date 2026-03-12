package uk.gov.onelogin.sharing.di.internal.shared

import android.content.Context
import dev.zacsweers.metro.createGraphFactory
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.di.api.presenter.PresenterCredentialSdk
import uk.gov.onelogin.sharing.di.api.shared.CredentialSharingAppGraph
import uk.gov.onelogin.sharing.di.api.shared.CredentialSharingSdk
import uk.gov.onelogin.sharing.di.api.verifier.VerifierCredentialSdk
import uk.gov.onelogin.sharing.di.internal.presenter.PresenterCredentialSdkImpl
import uk.gov.onelogin.sharing.di.internal.verifier.VerifierCredentialSdkImpl

class CredentialSharingSdkImpl(applicationContext: Context, logger: Logger) : CredentialSharingSdk {

    private val _appGraph: CredentialSharingAppGraph =
        createGraphFactory<CredentialSharingAppGraph.Factory>()
            .create(applicationContext, logger)

    override val appGraph: CredentialSharingAppGraph = _appGraph

    override val presenterCredentialSdk: PresenterCredentialSdk =
        PresenterCredentialSdkImpl(appGraph)

    override val verifierCredentialSdk: VerifierCredentialSdk =
        VerifierCredentialSdkImpl(appGraph)
}
