package uk.gov.onelogin.sharing.analytics

import app.cash.turbine.test
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.namedTestValues
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.namedTestValuesIn
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import kotlin.test.Test
import kotlin.test.assertTrue
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
import uk.gov.onelogin.sharing.analytics.AnalyticsEventTestData.sampleLegacyScreenEvent

@RunWith(TestParameterInjector::class)
class SystemAnalyticsLoggerTest {

    private val logger = SystemLogger()

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
        @TestParameter input: SendEventTestData = namedTestValuesIn(testInput)
    ) = runTest {
        val (shouldSend, isEnabled, assertion) = input

        analyticsLogger.setEnabled(isEnabled)

        analyticsLogger.logEvent(shouldSend, sampleLegacyScreenEvent)
        advanceUntilIdle()

        assertThat(
            logger.size,
            equalTo(1)
        )

        assertTrue(
            "Failed assertion due to logger contents: $logger"
        ) { assertion(logger) }
    }

    data class SendEventTestData(
        val shouldSend: Boolean,
        val isEnabled: Boolean,
        val assertion: (SystemLogger) -> Boolean
    )

    companion object {
        private val containsEventState: (LogEntry) -> Boolean = { entry ->
            entry.message.contains("Received analytics event: $sampleLegacyScreenEvent")
        }
        private val containsUnloggableEvent: (LogEntry) -> Boolean = { entry ->
            entry.message.contains(
                "Received an event that shouldn't be logged!"
            )
        }

        val testInput: Map<String, SendEventTestData> = mapOf(
            "Disabled logger that shouldn't send events, doesn't" to SendEventTestData(
                shouldSend = false,
                isEnabled = false
            ) {
                it.any(containsUnloggableEvent) &&
                    !it.any(containsEventState)
            },
            "Enabled logger that shouldn't send events, doesn't" to SendEventTestData(
                shouldSend = false,
                isEnabled = true
            ) {
                it.any(containsUnloggableEvent) &&
                    !it.any(containsEventState)
            },
            "Disabled logger that should send events, doesn't" to SendEventTestData(
                shouldSend = true,
                isEnabled = false
            ) {
                it.any(containsUnloggableEvent) &&
                    !it.any(containsEventState)
            },
            "Enabled logger that should send events, does" to SendEventTestData(
                shouldSend = true,
                isEnabled = true
            ) {
                !it.any(containsUnloggableEvent) &&
                    it.any(containsEventState)
            }
        )
    }
}
