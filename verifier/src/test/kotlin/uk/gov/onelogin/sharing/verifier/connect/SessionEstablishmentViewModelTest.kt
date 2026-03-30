package uk.gov.onelogin.sharing.verifier.connect

import androidx.lifecycle.SavedStateHandle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.google.testing.junit.testparameterinjector.TestParameters
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.core.presentation.permissions.FakeMultiplePermissionsState
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
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

    private fun createViewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()) =
        SessionEstablishmentViewModel(
            logger = logger,
            savedStateHandle = savedStateHandle,
            verifierOrchestrator = FakeOrchestrator()
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
