package uk.gov.onelogin.sharing.verifier.connect

import app.cash.turbine.test
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.core.presentation.bluetooth.BluetoothSessionError
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.session.SessionError
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(TestParameterInjector::class)
class SessionEstablishmentViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    val logger = SystemLogger()

    private lateinit var viewModel: SessionEstablishmentViewModel

    private fun createViewModel(orchestrator: FakeOrchestrator = FakeOrchestrator()) =
        SessionEstablishmentViewModel(
            logger = logger,
            orchestrator = orchestrator
        )

    @Test
    fun `navigates to error when session state is Complete Failed`() = runTest(
        mainDispatcherRule.testDispatcher
    ) {
        val verifierState =
            MutableStateFlow<VerifierSessionState>(VerifierSessionState.NotStarted)
        val orchestrator = FakeOrchestrator(initialVerifierState = verifierState)
        viewModel = createViewModel(orchestrator = orchestrator)

        viewModel.navEvents.test {
            verifierState.value = VerifierSessionState.Complete.Failed(
                SessionError(message = "error", exception = Exception())
            )
            assertEquals(
                ConnectWithHolderDeviceNavEvent.NavigateToError(
                    BluetoothSessionError.BluetoothConnectionError
                ),
                awaitItem()
            )
        }
    }
}
