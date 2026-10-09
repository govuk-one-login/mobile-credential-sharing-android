package uk.gov.onelogin.sharing.testapp.home

import app.cash.turbine.test
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.namedTestValuesIn
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.junit.Rule
import org.junit.runner.RunWith
import uk.gov.logging.api.v3dot1.logger.asLegacyEvent
import uk.gov.logging.api.v3dot1.model.TrackEvent
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters
import uk.gov.onelogin.sharing.analytics.StoredAnalyticsLogger
import uk.gov.onelogin.sharing.analytics.StoredAnalyticsLoggerMatchers.hasIgnoredEvents
import uk.gov.onelogin.sharing.analytics.StoredAnalyticsLoggerMatchers.hasSentEvents
import uk.gov.onelogin.sharing.analytics.SystemAnalyticsLogger
import uk.gov.onelogin.sharing.analytics.SystemLoggerMatchers.containsEventState
import uk.gov.onelogin.sharing.analytics.SystemLoggerMatchers.containsUnloggableEvent
import uk.gov.onelogin.sharing.core.MainDispatcherRule

@RunWith(TestParameterInjector::class)
class TestAppViewModelTest {
    private val analyticsLogger by lazy {
        StoredAnalyticsLogger()
    }
    private val viewModel by lazy {
        TestAppViewModel(
            analyticsLogger = analyticsLogger
        )
    }

    @Test
    fun `Sends screen view events to log`() = runTest {
        viewModel.sendScreenEvent().join()

        assertThat(
            analyticsLogger,
            hasSentEvents(contains(TestAppViewModelTestData.expectedScreenEvent))
        )
    }

    @Test
    fun `Updating navigation event also passes analytics events to ignore`(
        @TestParameter navigationEvent: TestAppViewModel.NavigationEvent = namedTestValuesIn(
            navigationEvents
        )
    ) = runTest {
        val expectedEvent = TrackEvent.Button(
            navigationEvent.buttonName,
            walletSharingRequiredParameters
        ).asLegacyEvent()
        viewModel.update(navigationEvent).join()

        assertThat(
            analyticsLogger,
            hasIgnoredEvents(contains(expectedEvent))
        )
    }

    @Test
    fun `Updating navigation event affects the internal shared flow`(
        @TestParameter navigationEvent: TestAppViewModel.NavigationEvent = namedTestValuesIn(
            navigationEvents
        )
    ) = runTest {
        viewModel.events.test {
            viewModel.update(navigationEvent).join()

            assertThat(
                expectMostRecentItem(),
                equalTo(navigationEvent)
            )
        }
    }

    companion object {
        val navigationEvents = mapOf(
            "Holder" to TestAppViewModel.NavigationEvent.Holder,
            "Verifier" to TestAppViewModel.NavigationEvent.Verifier
        )
    }
}
