package uk.gov.onelogin.sharing.holder.presentation

import app.cash.turbine.test
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.google.testing.junit.testparameterinjector.TestParameters
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.cryptoService.scanner.FakeQrParser
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(TestParameterInjector::class)
class HolderWelcomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val logger = SystemLogger()

    val qrData = "QR code"
    private var initialHolderState: HolderSessionState = HolderSessionState.PresentingEngagement(
        qrData
    )

    private val orchestrator by lazy {
        FakeOrchestrator(
            parser = FakeQrParser(),
            initialHolderState = MutableStateFlow(initialHolderState)
        )
    }

    private val viewModel by lazy {
        HolderWelcomeViewModel(
            logger = logger,
            orchestrator = orchestrator,
            dispatcher = mainDispatcherRule.testDispatcher
        )
    }

    @Test
    fun `Provides QR data during the 'PresentingEngagement' session state`() = runTest(
        mainDispatcherRule.testDispatcher
    ) {
            viewModel.uiState.test {
                assertThat(
                    awaitItem().qrData,
                    equalTo(qrData)
                )
            }
        }

    @Test
    fun `Has no QR data for inapplicable session states`() = runTest(
        mainDispatcherRule.testDispatcher
    ) {
        initialHolderState = HolderSessionState.NotStarted

        viewModel.uiState.test {
            assertThat(
                awaitItem().qrData,
                nullValue(String::class.java)
            )
        }
    }

    @TestParameters(valuesProvider = HolderScreenEventsFromState::class)
    @Test
    fun `Navigation events occur due to session state`(
        state: HolderSessionState,
        expected: HolderScreenEvents
    ) = runTest(mainDispatcherRule.testDispatcher) {
        initialHolderState = state

        viewModel.navEvents.test {
            assertThat(
                awaitItem(),
                equalTo(expected)
            )
        }
    }
}
