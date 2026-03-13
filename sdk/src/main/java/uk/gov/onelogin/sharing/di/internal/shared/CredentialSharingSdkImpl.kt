package uk.gov.onelogin.sharing.di.internal.shared

import android.content.Context
import dev.zacsweers.metro.createGraphFactory
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.di.api.presenter.PresentCredentialGraph
import uk.gov.onelogin.sharing.di.api.presenter.PresentCredentialSdk
import uk.gov.onelogin.sharing.di.api.shared.CredentialSharingAppGraph
import uk.gov.onelogin.sharing.di.api.shared.CredentialSharingSdk
import uk.gov.onelogin.sharing.di.api.verifier.VerifyCredentialGraph
import uk.gov.onelogin.sharing.di.api.verifier.VerifyCredentialSdk
import uk.gov.onelogin.sharing.di.internal.presenter.PresentCredentialSdkImpl
import uk.gov.onelogin.sharing.di.internal.verifier.VerifyCredentialSdkImpl

class CredentialSharingSdkImpl(applicationContext: Context, logger: Logger) : CredentialSharingSdk {

    private val _appGraph: CredentialSharingAppGraph =
        createGraphFactory<CredentialSharingAppGraph.Factory>()
            .create(applicationContext, logger)

    override val appGraph: CredentialSharingAppGraph = _appGraph

    override val presentCredentialSdk: PresentCredentialSdk =
        PresentCredentialSdkImpl(
            appGraph = appGraph,
            presenterGraphFactory = createGraphFactory<PresentCredentialGraph.Factory>()
        )

    override val verifyCredentialSdk: VerifyCredentialSdk =
        VerifyCredentialSdkImpl(
            appGraph = appGraph,
            verifierGraphFactory = createGraphFactory<VerifyCredentialGraph.Factory>()
        )
}
