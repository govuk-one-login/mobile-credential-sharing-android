package uk.gov.onelogin.sharing.verifier.connect

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.google.testing.junit.testparameterinjector.TestParameters
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.core.presentation.bluetooth.BluetoothSessionError
import uk.gov.onelogin.sharing.core.presentation.permissions.FakeMultiplePermissionsState
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.session.SessionError
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceStateMatchers.hasPreviouslyRequestedPermission
import uk.gov.onelogin.sharing.verifier.connect.SessionEstablishmentViewModelMatchers.hasUiState
import uk.gov.onelogin.sharing.verifier.connect.parameters.PermissionsToLogMessages

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(TestParameterInjector::class)
class SessionEstablishmentViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    val logger = SystemLogger()

    private lateinit var viewModel: SessionEstablishmentViewModel

    private fun createViewModel(
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
        orchestrator: FakeOrchestrator = FakeOrchestrator()
    ) = SessionEstablishmentViewModel(
        logger = logger,
        savedStateHandle = savedStateHandle,
        verifierOrchestrator = orchestrator
    )

    @Test
    fun `should update hasRequestPermissions`(@TestParameter hasRequestedPermission: Boolean) {
        viewModel = createViewModel()
        viewModel.receive(
            ConnectWithHolderDeviceEvent.RequestedPermission(
                hasRequestedPermission
            )
        )
        assertThat(
            viewModel,
            hasUiState(
                hasPreviouslyRequestedPermission(hasRequestedPermission)
            )
        )
    }

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

    @OptIn(ExperimentalPermissionsApi::class)
    @Test
    @TestParameters(valuesProvider = PermissionsToLogMessages::class)
    fun `Permission updates are logged`(
        input: FakeMultiplePermissionsState,
        expectedMessage: String
    ) {
        viewModel = createViewModel()
        viewModel.receive(ConnectWithHolderDeviceEvent.UpdatePermission(input))

        assertTrue(
            "Couldn't find expected message in logger: $logger",
            expectedMessage in logger
        )
    }
}
