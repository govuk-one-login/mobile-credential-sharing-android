package uk.gov.onelogin.sharing.sdk.internal.shared

import android.content.Context
import dev.zacsweers.metro.createGraphFactory
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.sdk.api.presenter.PresentCredentialGraph
import uk.gov.onelogin.sharing.sdk.api.presenter.PresentCredentialSdk
import uk.gov.onelogin.sharing.sdk.api.shared.CredentialSharingAppGraph
import uk.gov.onelogin.sharing.sdk.api.shared.CredentialSharingSdk
import uk.gov.onelogin.sharing.sdk.api.verifier.VerifyCredentialGraph
import uk.gov.onelogin.sharing.sdk.api.verifier.VerifyCredentialSdk
import uk.gov.onelogin.sharing.sdk.internal.presenter.PresentCredentialSdkImpl
import uk.gov.onelogin.sharing.sdk.internal.verifier.VerifyCredentialSdkImpl

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
