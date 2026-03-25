package uk.gov.onelogin.sharing.verifier.connect

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.google.testing.junit.testparameterinjector.TestParameters
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.api.adapter.FakeBluetoothAdapterProvider
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStatus
import uk.gov.onelogin.sharing.bluetooth.ble.DEVICE_ADDRESS
import uk.gov.onelogin.sharing.bluetooth.ble.FakeBluetoothStateMonitor
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.core.presentation.permissions.FakeMultiplePermissionsState
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceEventStubs.permissionUpdateDenied
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceEventStubs.permissionUpdateGranted
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceStateMatchers.hasBluetoothEnabled
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceStateMatchers.hasPreviouslyRequestedPermission
import uk.gov.onelogin.sharing.verifier.connect.SessionEstablishmentViewModel.Companion.PREVIOUSLY_HAD_PERMISSIONS_KEY
import uk.gov.onelogin.sharing.verifier.connect.SessionEstablishmentViewModelMatchers.hasUiState
import uk.gov.onelogin.sharing.verifier.connect.parameters.BluetoothStatusesToEnabledFlag
import uk.gov.onelogin.sharing.verifier.connect.parameters.PermissionsToLogMessages
import uk.gov.onelogin.sharing.verifier.session.FakeVerifierSession
import uk.gov.onelogin.sharing.verifier.session.VerifierSessionState

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(TestParameterInjector::class)
class SessionEstablishmentViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    val bluetoothAdapterProvider = FakeBluetoothAdapterProvider(isEnabled = true)
    val logger = SystemLogger()
    val fakeBluetoothStateMonitor = FakeBluetoothStateMonitor()
    val fakeVerifierSession = FakeVerifierSession()

    lateinit var viewModel: SessionEstablishmentViewModel

    private fun createViewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()) =
        SessionEstablishmentViewModel(
            bluetoothAdapterProvider = bluetoothAdapterProvider,
            verifierSessionFactory = { fakeVerifierSession },
            logger = logger,
            bluetoothStatusMonitor = fakeBluetoothStateMonitor,
            savedStateHandle = savedStateHandle
        )

    @Test
    fun `init sets isBluetoothEnabled from adapter provider`(
        @TestParameter isBluetoothEnabled: Boolean
    ) {
        bluetoothAdapterProvider.setEnabled(isBluetoothEnabled)
        viewModel = createViewModel()
        assertThat(
            viewModel,
            hasUiState(
                hasBluetoothEnabled(isBluetoothEnabled)
            )
        )
    }

    @Test
    fun `Connecting to invalid configuration navigates to Bluetooth Configuration Error screen`() =
        runTest {
            viewModel = createViewModel()
            viewModel.navEvents.test {
                fakeVerifierSession.updateState(
                    VerifierSessionState.Invalid
                )

                assertEquals(
                    ConnectWithHolderDeviceNavEvent.NavigateToError(
                        ConnectWithHolderDeviceError.BluetoothConfigurationError
                    ),
                    awaitItem()
                )
            }
        }

    @Test
    fun `Failed to find GATT service navigates to Bluetooth Configuration Error screen`() =
        runTest {
            viewModel = createViewModel()
            viewModel.navEvents.test {
                fakeVerifierSession.updateState(
                    VerifierSessionState.ServiceNotFound
                )

                assertEquals(
                    ConnectWithHolderDeviceNavEvent.NavigateToError(
                        ConnectWithHolderDeviceError.BluetoothConfigurationError
                    ),
                    awaitItem()
                )
            }
        }

    @Test
    fun `navigates to error screen when bluetooth disconnects during session`() = runTest {
        viewModel = createViewModel()
        viewModel.navEvents.test {
            fakeVerifierSession.updateState(
                VerifierSessionState.Connected(DEVICE_ADDRESS)
            )

            fakeVerifierSession.updateState(
                VerifierSessionState.Disconnected(DEVICE_ADDRESS, false)
            )

            assertEquals(1, fakeVerifierSession.stopCalls)

            assertEquals(
                ConnectWithHolderDeviceNavEvent.NavigateToError(
                    ConnectWithHolderDeviceError.BluetoothConnectionError
                ),
                awaitItem()
            )
        }
    }

    @Test
    fun `navigates to error screen when bluetooth disconnects before session starts`() = runTest {
        viewModel = createViewModel()
        viewModel.navEvents.test {
            fakeVerifierSession.updateState(
                VerifierSessionState.Disconnected(DEVICE_ADDRESS, false)
            )

            assertEquals(0, fakeVerifierSession.stopCalls)

            assertEquals(
                ConnectWithHolderDeviceNavEvent.NavigateToError(
                    ConnectWithHolderDeviceError.BluetoothConnectionError
                ),
                awaitItem()
            )
        }
    }

    @Test
    fun `navigates to error screen when bluetooth is disabled`() = runTest {
        viewModel = createViewModel()

        viewModel.navEvents.test {
            fakeBluetoothStateMonitor.emit(BluetoothStatus.OFF)

            assertEquals(1, fakeVerifierSession.stopCalls)

            assertEquals(
                ConnectWithHolderDeviceNavEvent.NavigateToError(
                    ConnectWithHolderDeviceError.BluetoothDisabledError
                ),
                awaitItem()
            )
        }
    }

    @Test
    fun `navigates to error screen when permissions are revoked`() = runTest {
        viewModel = createViewModel(
            SavedStateHandle(
                mapOf(PREVIOUSLY_HAD_PERMISSIONS_KEY to true)
            )
        )

        viewModel.navEvents.test {
            viewModel.receive(permissionUpdateDenied)

            assertEquals(1, fakeVerifierSession.stopCalls)

            assertEquals(
                ConnectWithHolderDeviceNavEvent.NavigateToError(
                    ConnectWithHolderDeviceError.BluetoothPermissionsError
                ),
                awaitItem()
            )
        }
    }

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

    @Test
    @TestParameters(valuesProvider = BluetoothStatusesToEnabledFlag::class)
    fun `Bluetooth status maps to Bluetooth enablement flag`(
        status: BluetoothStatus,
        assertion: Matcher<ConnectWithHolderDeviceState>
    ) = runTest {
        viewModel = createViewModel()

        fakeBluetoothStateMonitor.emit(status)

        assertThat(
            viewModel,
            hasUiState(assertion)
        )
    }

    @Test
    fun `showErrorScreen is false when isSessionEnd set true for disconnects`() = runTest {
        viewModel = createViewModel()
        viewModel.navEvents.test {
            fakeVerifierSession.updateState(
                VerifierSessionState.Connected(DEVICE_ADDRESS)
            )

            fakeVerifierSession.updateState(
                VerifierSessionState.Disconnected(DEVICE_ADDRESS, true)
            )

            assertEquals(1, fakeVerifierSession.stopCalls)

            expectNoEvents()
        }
    }
}
