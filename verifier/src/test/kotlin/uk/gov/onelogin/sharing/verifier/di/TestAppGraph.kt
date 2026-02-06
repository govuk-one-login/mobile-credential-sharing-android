package uk.gov.onelogin.sharing.verifier.di

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import dev.zacsweers.metro.createGraphFactory
import uk.gov.logging.api.Logger
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.di.CredentialSharingAppGraph

/**
 * Helper function to create a [CredentialSharingAppGraph] instance for use in tests.
 *
 * Metro does not support testFixtures so this has to be created in the test source set.
 *
 * Uses the Metro library's [createGraphFactory] to instantiate the test graph,
 * allowing for the injection of test components.
 *
 * @param applicationContext The [Context] to be used in the graph. Defaults to the application
 * context provided by [ApplicationProvider].
 * @param logger The [Logger] implementation to be used. Defaults to [SystemLogger].
 * @return A configured [CredentialSharingAppGraph]
 */
fun createTestGraph(
    applicationContext: Context = ApplicationProvider.getApplicationContext(),
    logger: Logger = SystemLogger()
): CredentialSharingAppGraph = createGraphFactory<CredentialSharingAppGraph.Factory>()
    .create(
        applicationContext = applicationContext,
        logger = logger
    )
