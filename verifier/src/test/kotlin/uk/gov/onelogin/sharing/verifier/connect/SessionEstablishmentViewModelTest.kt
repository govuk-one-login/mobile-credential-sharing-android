package uk.gov.onelogin.sharing.verifier.connect

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.google.testing.junit.testparameterinjector.TestParameters
import io.mockk.every
import io.mockk.mockk
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.logging.testdouble.LogEntry
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.api.adapter.FakeBluetoothAdapterProvider
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStatus
import uk.gov.onelogin.sharing.bluetooth.api.scanner.BluetoothScanner
import uk.gov.onelogin.sharing.bluetooth.api.scanner.FakeAndroidBluetoothScanner
import uk.gov.onelogin.sharing.bluetooth.api.scanner.FakeAndroidBluetoothScanner.StubData.dummyByteArray
import uk.gov.onelogin.sharing.bluetooth.api.scanner.ScanEvent
import uk.gov.onelogin.sharing.bluetooth.api.scanner.ScannerFailure
import uk.gov.onelogin.sharing.bluetooth.ble.DEVICE_ADDRESS
import uk.gov.onelogin.sharing.bluetooth.ble.FakeBluetoothStateMonitor
import uk.gov.onelogin.sharing.bluetooth.scanner.DummyBluetoothScanner
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.core.presentation.permissions.FakeMultiplePermissionsState
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.toByteArray
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceEventStubs.permissionUpdateDenied
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceEventStubs.permissionUpdateGranted
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceEventStubs.startScanningDummyServiceUuid
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceStateMatchers.hasBluetoothEnabled
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceStateMatchers.hasPreviouslyRequestedPermission
import uk.gov.onelogin.sharing.verifier.connect.SessionEstablishmentViewModel.Companion.PREVIOUSLY_HAD_PERMISSIONS_KEY
import uk.gov.onelogin.sharing.verifier.connect.SessionEstablishmentViewModelMatchers.hasUiState
import uk.gov.onelogin.sharing.verifier.connect.parameters.BluetoothStatusesToEnabledFlag
import uk.gov.onelogin.sharing.verifier.connect.parameters.EncodedEngagementToState
import uk.gov.onelogin.sharing.verifier.connect.parameters.PermissionsToLogMessages
import uk.gov.onelogin.sharing.verifier.session.FakeVerifierSession
import uk.gov.onelogin.sharing.verifier.session.VerifierSessionState

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(TestParameterInjector::class)
class SessionEstablishmentViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    val bluetoothAdapterProvider = FakeBluetoothAdapterProvider(isEnabled = true)
    val scanner = FakeAndroidBluetoothScanner()
    val logger = SystemLogger()
    val fakeBluetoothStateMonitor = FakeBluetoothStateMonitor()
    val fakeVerifierSession = FakeVerifierSession()

    lateinit var viewModel: SessionEstablishmentViewModel

    private fun createViewModel(
        scanner: BluetoothScanner,
        savedStateHandle: SavedStateHandle = SavedStateHandle()
    ) = SessionEstablishmentViewModel(
        bluetoothAdapterProvider = bluetoothAdapterProvider,
        scanner = scanner,
        dispatcher = mainDispatcherRule.testDispatcher,
        logger = logger,
        bluetoothStatusMonitor = fakeBluetoothStateMonitor,
        verifierSessionFactory = { fakeVerifierSession },
        savedStateHandle = savedStateHandle
    )

    @Test
    fun `init sets isBluetoothEnabled from adapter provider`(
        @TestParameter isBluetoothEnabled: Boolean
    ) {
        bluetoothAdapterProvider.setEnabled(isBluetoothEnabled)
        viewModel = createViewModel(scanner)
        assertThat(
            viewModel,
            hasUiState(
                hasBluetoothEnabled(isBluetoothEnabled)
            )
        )
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Test
    fun `scanForDevice calls scanner with provided uuid`() = runTest {
        viewModel = createViewModel(scanner)
        viewModel.receive(permissionUpdateGranted)
        viewModel.receive(startScanningDummyServiceUuid)

        assertEquals(1, scanner.scanCalls)
        assertArrayEquals(dummyByteArray, scanner.lastUuid)
    }

    @Test
    fun `scanForDevice handles DeviceFound ScanEvent and logs it`() = runTest {
        val bluetoothDevice = mockk<BluetoothDevice>()
        every { bluetoothDevice.address } returns DEVICE_ADDRESS

        val scanner = BluetoothScanner.of(ScanEvent.DeviceFound(bluetoothDevice))

        val viewModel = createViewModel(scanner)

        viewModel.receive(permissionUpdateGranted)

        val uuid = UUID.randomUUID()

        viewModel.receive(ConnectWithHolderDeviceEvent.StartScanning(uuid.toByteArray()))
        runCurrent()

        assert(
            "Bluetooth device found: $DEVICE_ADDRESS" in logger
        ) {
            "Didn't find expected message: $logger"
        }
    }

    @Test
    fun `scanForDevice handles ScanFailure ScanEvent and logs it`() = runTest {
        val scanFailure = ScannerFailure.ALREADY_STARTED_SCANNING

        val scanner = BluetoothScanner.of(
            ScanEvent.ScanFailed(scanFailure)
        )

        val viewModel = createViewModel(scanner)

        viewModel.receive(permissionUpdateGranted)
        viewModel.receive(startScanningDummyServiceUuid)
        runCurrent()

        assert(
            "Scan failed: ${ScannerFailure.ALREADY_STARTED_SCANNING}" in logger
        ) {
            "Cannot find expected log message: $logger"
        }
    }

    @Test
    fun `stopScanning logs and cancels an active scan job`() = runTest {
        var flowClosed = false

        val scanner = BluetoothScanner.from(
            callbackFlow {
                awaitClose { flowClosed = true }
            }
        )

        val viewModel = createViewModel(scanner)

        viewModel.receive(permissionUpdateGranted)
        viewModel.receive(startScanningDummyServiceUuid)

        runCurrent()

        viewModel.receive(ConnectWithHolderDeviceEvent.StopScanning)
        runCurrent()

        assertTrue(
            "Expected scan flow to be closed after cancel",
            flowClosed
        )
    }

    @Test
    fun `scanForDevice times out when no results emitted`() = runTest {
        val scanner = BluetoothScanner.from(
            callbackFlow {
                awaitCancellation()
            }
        )

        val viewModel = createViewModel(scanner)
        viewModel.receive(permissionUpdateGranted)
        viewModel.receive(startScanningDummyServiceUuid)

        runCurrent()

        advanceTimeBy(SessionEstablishmentViewModel.SCAN_PERIOD)
        advanceUntilIdle()

        assert(
            logger.any {
                it is LogEntry.Message && it.message.startsWith(
                    "kotlinx.coroutines.TimeoutCancellationException:"
                )
            }
        ) {
            "Cannot find expected error message: $logger"
        }
    }

    @Test
    fun `scanForDevice on ScanEvent ScanFailure navigates to generic error screen`() = runTest {
        val scanFailure = ScannerFailure.ALREADY_STARTED_SCANNING

        val scanner = BluetoothScanner.of(
            ScanEvent.ScanFailed(scanFailure)
        )

        val viewModel = createViewModel(scanner)

        viewModel.navEvents.test {
            viewModel.receive(permissionUpdateGranted)
            viewModel.receive(startScanningDummyServiceUuid)

            assertEquals(
                ConnectWithHolderDeviceNavEvent.NavigateToError(
                    ConnectWithHolderDeviceError.GenericError
                ),
                awaitItem()
            )
        }
    }

    @Test
    fun `Connecting to invalid configuration navigates to Bluetooth Configuration Error screen`() =
        runTest {
            viewModel = createViewModel(DummyBluetoothScanner)
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
            viewModel = createViewModel(DummyBluetoothScanner)
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
        viewModel = createViewModel(DummyBluetoothScanner)
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
        viewModel = createViewModel(DummyBluetoothScanner)
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
        viewModel = createViewModel(DummyBluetoothScanner)

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
            DummyBluetoothScanner,
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
        viewModel = createViewModel(scanner)
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
        viewModel = createViewModel(scanner)
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
        viewModel = createViewModel(scanner)

        fakeBluetoothStateMonitor.emit(status)

        assertThat(
            viewModel,
            hasUiState(assertion)
        )
    }

    @Test
    @TestParameters(valuesProvider = EncodedEngagementToState::class)
    fun `Updating encoded data affects the UI state`(
        input: String,
        assertion: Matcher<ConnectWithHolderDeviceState>
    ) = runTest {
        viewModel = createViewModel(scanner)

        viewModel.receive(
            ConnectWithHolderDeviceEvent.UpdateEngagementData(input)
        )

        assertThat(
            viewModel,
            hasUiState(assertion)
        )
    }

    @Test
    fun `showErrorScreen is false when isSessionEnd set true for disconnects`() = runTest {
        viewModel = createViewModel(DummyBluetoothScanner)
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
