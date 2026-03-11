package uk.gov.onelogin.sharing.di.internal

import android.content.Context
import dev.zacsweers.metro.createGraphFactory
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.CredentialSharingSdk
import uk.gov.onelogin.sharing.PresenterCredentialSdk
import uk.gov.onelogin.sharing.VerifierCredentialSdk
import uk.gov.onelogin.sharing.di.CredentialSharingAppGraph

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
