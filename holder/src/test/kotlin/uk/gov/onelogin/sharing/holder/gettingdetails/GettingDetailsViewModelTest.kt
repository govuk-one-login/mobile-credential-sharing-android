package uk.gov.onelogin.sharing.holder.gettingdetails

import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.junit.Rule
import org.junit.Test
import uk.gov.logging.api.v3.v3dot1.logger.asLegacyEvent
import uk.gov.logging.api.v3dot1.model.ViewEvent
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters
import uk.gov.onelogin.sharing.analytics.StoredAnalyticsLogger
import uk.gov.onelogin.sharing.analytics.StoredAnalyticsLoggerMatchers.hasSentEvents
import uk.gov.onelogin.sharing.core.MainDispatcherRule

class GettingDetailsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val analyticsLogger = StoredAnalyticsLogger()

    private val viewModel by lazy {
        GettingDetailsViewModel(
            analyticsLogger = analyticsLogger,
            dispatcher = mainDispatcherRule.testDispatcher
        )
    }

    @Test
    fun `Logs screen view event`() = runTest(mainDispatcherRule.testDispatcher) {
        viewModel.logScreenView().join()

        assertThat(
            analyticsLogger,
            hasSentEvents(
                contains(
                    ViewEvent.Screen(
                        name = "Getting list of details",
                        id = "TODO-PROCESSING-ESTABLISHMENT-SCREEN-ID",
                        params = walletSharingRequiredParameters
                    ).asLegacyEvent()
                )
            )
        )
    }
}
