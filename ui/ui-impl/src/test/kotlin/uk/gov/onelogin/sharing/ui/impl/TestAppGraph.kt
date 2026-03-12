package uk.gov.onelogin.sharing.ui.impl

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import dev.zacsweers.metro.createGraphFactory
import uk.gov.logging.api.Logger
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.di.api.presenter.PresenterCredentialGraph
import uk.gov.onelogin.sharing.di.api.shared.CredentialSharingAppGraph
import uk.gov.onelogin.sharing.di.api.verifier.VerifierCredentialGraph
import uk.gov.onelogin.sharing.orchestration.FakeCredentialProvider
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierConfigStub.verifierConfigStub

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
    logger: Logger = SystemLogger()
): CredentialSharingAppGraph = createGraphFactory<CredentialSharingAppGraph.Factory>()
    .create(
        applicationContext = applicationContext,
        logger = logger
    )

fun createTestHolderGraph(appGraph: CredentialSharingAppGraph): PresenterCredentialGraph =
    createGraphFactory<PresenterCredentialGraph.Factory>()
        .create(appGraph = appGraph, credentialProvider = FakeCredentialProvider())

fun createTestVerifierGraph(appGraph: CredentialSharingAppGraph): VerifierCredentialGraph =
    createGraphFactory<VerifierCredentialGraph.Factory>()
        .create(
            appGraph = appGraph,
            verifierConfig = verifierConfigStub
        )
