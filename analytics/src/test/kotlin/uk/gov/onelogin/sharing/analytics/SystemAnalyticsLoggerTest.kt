package uk.gov.onelogin.sharing.analytics

import app.cash.turbine.test
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.namedTestValuesIn
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.junit.runner.RunWith
import uk.gov.logging.api.v3.MemorisedLogger
import uk.gov.logging.api.v3.matchers.MemorisedLoggerMatchers.hasSize
import uk.gov.onelogin.sharing.analytics.AnalyticsEventTestData.sampleLegacyScreenEvent
import uk.gov.onelogin.sharing.analytics.SystemLoggerMatchers.containsEventState
import uk.gov.onelogin.sharing.analytics.SystemLoggerMatchers.containsUnloggableEvent
import kotlin.test.Test

@RunWith(TestParameterInjector::class)
class SystemAnalyticsLoggerTest {

    private val logger = MemorisedLogger()

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

        assertThat(
            logger,
            allOf(
                hasSize(1),
                assertion
            )
        )
    }

    data class SendEventTestData(
        val shouldSend: Boolean,
        val isEnabled: Boolean,
        val assertion: Matcher<in MemorisedLogger>
    )

    companion object {
        val testInput: Map<String, SendEventTestData> = mapOf(
            "Disabled logger that shouldn't send events, doesn't" to SendEventTestData(
                shouldSend = false,
                isEnabled = false,
                containsUnloggableEvent()
            ),
            "Enabled logger that shouldn't send events, doesn't" to SendEventTestData(
                shouldSend = false,
                isEnabled = true,
                containsUnloggableEvent()
            ),
            "Disabled logger that should send events, doesn't" to SendEventTestData(
                shouldSend = true,
                isEnabled = false,
                containsUnloggableEvent()
            ),
            "Enabled logger that should send events, does" to SendEventTestData(
                shouldSend = true,
                isEnabled = true,
                containsEventState()
            )
        )
    }
}
