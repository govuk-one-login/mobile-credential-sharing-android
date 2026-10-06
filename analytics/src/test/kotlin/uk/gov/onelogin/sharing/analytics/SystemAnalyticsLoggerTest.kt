package uk.gov.onelogin.sharing.analytics

import app.cash.turbine.test
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.namedTestValues
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.runner.RunWith
import uk.gov.logging.api.analytics.parameters.data.TaxonomyLevel2
import uk.gov.logging.api.v3dot1.logger.asLegacyEvent
import uk.gov.logging.api.v3dot1.model.RequiredParameters
import uk.gov.logging.api.v3dot1.model.ViewEvent
import uk.gov.logging.testdouble.v2.LogEntry
import uk.gov.logging.testdouble.v2.SystemLogger
import kotlin.test.Test
import kotlin.test.assertTrue

@RunWith(TestParameterInjector::class)
class SystemAnalyticsLoggerTest {

    private val logger = SystemLogger()

    private val event = ViewEvent.Screen(
        name = "Unit test",
        id = "unitTest",
        params = RequiredParameters(
            taxonomyLevel2 = TaxonomyLevel2.WALLET
        )
    ).asLegacyEvent()
    private val containsEventState: (LogEntry) -> Boolean = { entry ->
        entry.message.contains("Received analytics event: $event")
    }

    private val analyticsLogger by lazy {
        SystemAnalyticsLogger(
            logger = logger
        )
    }

    @Test
    fun `Internal state flow changes via 'setEnabled'`(@TestParameter input: Boolean) = runTest {
        analyticsLogger.setEnabled(input)

        analyticsLogger.isEnabled.test {
            assertThat(
                expectMostRecentItem(),
                equalTo(input)
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Logging events depend on internal state`(
        @TestParameter input: Triple<Boolean, Boolean, (SystemLogger) -> Boolean> = namedTestValues(
            "Disabled logger that shouldn't send events, doesn't" to Triple(false, false) {
                !it.any(containsEventState)
            },
            "Enabled logger that shouldn't send events, doesn't" to Triple(false, true) {
                !it.any(containsEventState)
            },
            "Disabled logger that should send events, doesn't" to Triple(true, false) {
                !it.any(containsEventState)
            },
            "Enabled logger that should send events, does" to Triple(true, true) {
                it.any(containsEventState)
            },
        )
    ) = runTest {
        val (shouldSend, isEnabled, assertion) = input

        analyticsLogger.setEnabled(isEnabled)

        analyticsLogger.logEvent(shouldSend, event)
        advanceUntilIdle()

        assertTrue(
            "Failed assertion due to logger contents: $logger"
        ) { assertion(logger) }
    }
}