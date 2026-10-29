package uk.gov.onelogin.sharing.holder.loading

import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.logging.api.v3.v3dot1.logger.asLegacyEvent
import uk.gov.logging.api.v3dot1.model.ViewEvent
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters
import uk.gov.onelogin.sharing.analytics.StoredAnalyticsLogger
import uk.gov.onelogin.sharing.analytics.StoredAnalyticsLoggerMatchers.hasSentEvents
import uk.gov.onelogin.sharing.core.MainDispatcherRule

@RunWith(TestParameterInjector::class)
class LoadingViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val analyticsLogger = StoredAnalyticsLogger()

    private val viewModel by lazy {
        LoadingViewModel(
            analyticsLogger = analyticsLogger,
            dispatcher = mainDispatcherRule.testDispatcher
        )
    }

    @Test
    fun `Logs the screen view event for the given type`(
        @TestParameter type: LoadingScreenType
    ) = runTest(mainDispatcherRule.testDispatcher) {
        viewModel.logScreenView(type).join()

        assertThat(
            analyticsLogger,
            hasSentEvents(
                contains(
                    ViewEvent.Screen(
                        name = type.screenName,
                        id = type.firebaseScreenId,
                        params = walletSharingRequiredParameters
                    ).asLegacyEvent()
                )
            )
        )
    }
}
