package uk.gov.onelogin.sharing.ui.impl

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import dev.zacsweers.metro.createGraphFactory
import java.security.cert.X509Certificate
import uk.gov.logging.api.v2.Logger
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.orchestration.CredentialProvider
import uk.gov.onelogin.sharing.orchestration.FakeCredentialProvider
import uk.gov.onelogin.sharing.orchestration.verificationrequest.VerifierConfig
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierConfigStub.verifierConfigStub
import uk.gov.onelogin.sharing.prerequisites.api.permissions.PermissionChecker
import uk.gov.onelogin.sharing.sdk.api.presenter.PresentCredentialGraph
import uk.gov.onelogin.sharing.sdk.api.shared.CredentialSharingAppGraph
import uk.gov.onelogin.sharing.sdk.api.verifier.VerifyCredentialGraph
import uk.gov.onelogin.sharing.verification.CredentialVerificationGraph

/**
 * Helper function to create a [CredentialSharingAppGraph] instance for use in tests.
 *
 * Metro does not support testFixtures so this has to be created in the test source set.
 *
 * @param applicationContext The [Context] to be used in the graph. Defaults to the application
 * context provided by [ApplicationProvider].
 * @param logger The [Logger] implementation to be used. Defaults to [SystemLogger].
 * @return A configured [CredentialSharingAppGraph]
 */
fun createTestAppGraph(
    applicationContext: Context = ApplicationProvider.getApplicationContext(),
    logger: Logger = SystemLogger(),
    checker: PermissionChecker = PermissionChecker { emptyList() }
): CredentialSharingAppGraph = createGraphFactory<CredentialSharingAppGraph.Factory>()
    .create(
        applicationContext = applicationContext,
        logger = logger,
        permissionChecker = checker
    )

fun createTestHolderGraph(
    appGraph: CredentialSharingAppGraph,
    credentialProvider: CredentialProvider = FakeCredentialProvider()
): PresentCredentialGraph = createGraphFactory<PresentCredentialGraph.Factory>().create(
    appGraph = appGraph,
    credentialProvider = credentialProvider
)

fun createTestVerifierGraph(
    appGraph: CredentialSharingAppGraph,
    credentialVerificationGraph: CredentialVerificationGraph,
    config: VerifierConfig = verifierConfigStub
): VerifyCredentialGraph = createGraphFactory<VerifyCredentialGraph.Factory>().create(
    appGraph = appGraph,
    credentialVerificationGraph = credentialVerificationGraph,
    verifierConfig = config
)

fun createTestCredentialVerificationGraph(
    certificate: X509Certificate
): CredentialVerificationGraph = createGraphFactory<CredentialVerificationGraph.Factory>()
    .create(certificate)
