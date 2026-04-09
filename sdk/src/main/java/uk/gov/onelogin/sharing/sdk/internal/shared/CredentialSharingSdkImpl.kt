package uk.gov.onelogin.sharing.sdk.internal.shared

import android.content.Context
import dev.zacsweers.metro.createGraphFactory
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.permission.PermissionCheckerV2
import uk.gov.onelogin.sharing.sdk.api.presenter.PresentCredentialGraph
import uk.gov.onelogin.sharing.sdk.api.presenter.PresentCredentialSdk
import uk.gov.onelogin.sharing.sdk.api.shared.CredentialSharingAppGraph
import uk.gov.onelogin.sharing.sdk.api.shared.CredentialSharingSdk
import uk.gov.onelogin.sharing.sdk.api.verifier.VerifyCredentialGraph
import uk.gov.onelogin.sharing.sdk.api.verifier.VerifyCredentialSdk
import uk.gov.onelogin.sharing.sdk.internal.presenter.PresentCredentialSdkImpl
import uk.gov.onelogin.sharing.sdk.internal.verifier.VerifyCredentialSdkImpl

class CredentialSharingSdkImpl(
    applicationContext: Context,
    logger: Logger,
    permissionChecker: PermissionCheckerV2
) : CredentialSharingSdk {

    private val _appGraph: CredentialSharingAppGraph =
        createGraphFactory<CredentialSharingAppGraph.Factory>()
            .create(
                applicationContext,
                logger,
                permissionChecker
            )

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
