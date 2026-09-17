package uk.gov.onelogin.sharing.sdk.internal.shared

import android.content.Context
import dev.zacsweers.metro.createGraphFactory
import java.security.cert.X509Certificate
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.orchestration.CredentialProvider
import uk.gov.onelogin.sharing.orchestration.verifier.auth.reader.ReaderAuthCredentialProvider
import uk.gov.onelogin.sharing.prerequisites.api.permissions.PermissionChecker
import uk.gov.onelogin.sharing.sdk.api.presenter.CredentialPresenter
import uk.gov.onelogin.sharing.sdk.api.presenter.PresentCredentialGraph
import uk.gov.onelogin.sharing.sdk.api.presenter.PresentCredentialSdk
import uk.gov.onelogin.sharing.sdk.api.shared.CredentialSharingAppGraph
import uk.gov.onelogin.sharing.sdk.api.shared.CredentialSharingSdk
import uk.gov.onelogin.sharing.sdk.api.verifier.VerifyCredentialGraph
import uk.gov.onelogin.sharing.sdk.api.verifier.VerifyCredentialSdk
import uk.gov.onelogin.sharing.sdk.internal.presenter.CredentialPresenterImpl
import uk.gov.onelogin.sharing.sdk.internal.presenter.PresentCredentialSdkImpl
import uk.gov.onelogin.sharing.sdk.internal.verifier.VerifyCredentialSdkImpl
import uk.gov.onelogin.sharing.verification.CredentialVerificationGraph

class CredentialSharingSdkImpl(
    applicationContext: Context,
    logger: Logger,
    permissionChecker: PermissionChecker,
    readerAuthCredentialFactory: ReaderAuthCredentialProvider.Factory
) : CredentialSharingSdk {

    private val _appGraph: CredentialSharingAppGraph =
        createGraphFactory<CredentialSharingAppGraph.Factory>()
            .create(
                applicationContext,
                logger,
                permissionChecker
            )

    override val appGraph: CredentialSharingAppGraph = _appGraph

    @Suppress("DEPRECATION")
    @Deprecated(
        message = "Use createCredentialPresenter instead",
        replaceWith = ReplaceWith(
            "createCredentialPresenter(credentialProvider, trustedReaderCertificates)"
        )
    )
    override val presentCredentialSdk: PresentCredentialSdk =
        PresentCredentialSdkImpl(
            appGraph = appGraph,
            presenterGraphFactory = createGraphFactory<PresentCredentialGraph.Factory>()
        )

    override fun createCredentialPresenter(
        credentialProvider: CredentialProvider,
        trustedReaderCertificates: List<X509Certificate>
    ): CredentialPresenter {
        val presenterGraphFactory = createGraphFactory<PresentCredentialGraph.Factory>()
        val orchestrator = presenterGraphFactory
            .create(appGraph, credentialProvider)
            .holderOrchestrator()

        return CredentialPresenterImpl(
            credentialProvider = credentialProvider,
            orchestrator = orchestrator,
            appGraph = appGraph
        )
    }

    override val verifyCredentialSdk: VerifyCredentialSdk =
        VerifyCredentialSdkImpl(
            appGraph = appGraph,
            verifierGraphFactory = createGraphFactory<VerifyCredentialGraph.Factory>(),
            credentialVerificationGraphFactory =
                createGraphFactory<CredentialVerificationGraph.Factory>(),
            readerAuthCredentialFactory = readerAuthCredentialFactory
        )
}
