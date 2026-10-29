package uk.gov.onelogin.sharing.holder.sharingdetails

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

class SharingDetailsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val analyticsLogger = StoredAnalyticsLogger()

    private val viewModel by lazy {
        SharingDetailsViewModel(
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
                        name = "Sharing your details",
                        id = "aedea082-0575-4f4e-babf-b2221d74756f",
                        params = walletSharingRequiredParameters
                    ).asLegacyEvent()
                )
            )
        )
    }
}
