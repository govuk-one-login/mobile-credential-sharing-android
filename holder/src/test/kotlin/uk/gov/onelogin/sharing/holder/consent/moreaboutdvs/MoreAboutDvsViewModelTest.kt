package uk.gov.onelogin.sharing.holder.consent.moreaboutdvs

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.junit.runner.RunWith
import uk.gov.logging.analytics.FakeAnalyticsLogger
import uk.gov.onelogin.sharing.holder.consent.moreaboutdvs.MoreAboutDvsTestData.expectedButtonEvent
import uk.gov.onelogin.sharing.holder.consent.moreaboutdvs.MoreAboutDvsTestData.expectedScreenEvent
import uk.gov.onelogin.sharing.holder.consent.moreaboutdvs.MoreAboutDvsUiStateMatchers.hasUiState
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentPresentationTestData.createConsentPresentation
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import kotlin.test.Test

@RunWith(AndroidJUnit4::class)
class MoreAboutDvsViewModelTest {

    private val analyticsLogger = FakeAnalyticsLogger()

    private val context: Context = ApplicationProvider.getApplicationContext()

    private var organizationName: String = "Stub-DVS Ltd."

    private val presentation by lazy {
        createConsentPresentation(
            organizationName = organizationName
        )
    }

    private val initialHolderState by lazy {
        HolderSessionState.AwaitingUserConsent(
            request = mockk(relaxed = true),
            presentation = presentation
        )
    }

    private val orchestrator by lazy {
        FakeOrchestrator(
            initialHolderState = MutableStateFlow(initialHolderState)
        )
    }

    private val viewModel by lazy {
        MoreAboutDvsViewModel(
            analyticsLogger = analyticsLogger,
            orchestrator = orchestrator
        )
    }

    @Test
    fun `UI state flow maps the ConsentPresentation`() = runTest {
        viewModel.uiState.test {
            assertThat(
                expectMostRecentItem(),
                hasUiState(presentation)
            )
        }
    }

    @Test
    fun `Sends a screen-level analytics event`() = runTest {
        viewModel.sendScreenEvent(context).join()

        assertThat(
            analyticsLogger,
            contains(expectedScreenEvent)
        )
    }

    @Test
    fun `Sends a button-level analytics event`() = runTest {
        viewModel.sendButtonEvent(context).join()

        assertThat(
            analyticsLogger,
            contains(expectedButtonEvent)
        )
    }
}
