package uk.gov.onelogin.sharing.verifier.connect

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.SavedStateHandle
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.api.adapter.BluetoothAdapterProvider
import uk.gov.onelogin.sharing.bluetooth.api.adapter.FakeBluetoothAdapterProvider.Companion.disabledBluetoothAdapter
import uk.gov.onelogin.sharing.bluetooth.api.adapter.FakeBluetoothAdapterProvider.Companion.enabledBluetoothAdapter
import uk.gov.onelogin.sharing.bluetooth.api.scanner.FakeAndroidBluetoothScanner
import uk.gov.onelogin.sharing.bluetooth.ble.FakeBluetoothStateMonitor
import uk.gov.onelogin.sharing.core.presentation.permissions.FakeMultiplePermissionsStateStubs.bluetoothPermissionsDenied
import uk.gov.onelogin.sharing.core.presentation.permissions.FakeMultiplePermissionsStateStubs.bluetoothPermissionsGranted
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceStateStubs.decodableDeniedState
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceStateStubs.decodableGrantedState
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceStateStubs.genericErrorState
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceStateStubs.undecodableState
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceStateStubs.validWithCorrectBluetoothSetup
import uk.gov.onelogin.sharing.verifier.session.FakeVerifierSession
import uk.gov.onelogin.sharing.verifier.session.VerifierSessionState

@OptIn(ExperimentalPermissionsApi::class)
@RunWith(AndroidJUnit4::class)
class ConnectWithHolderDeviceScreenTest {

    @get:Rule
    val composeTestRule = ConnectWithHolderDeviceRule(createComposeRule())

    private lateinit var testViewModel: SessionEstablishmentViewModel

    private val logger = SystemLogger()

    fun createViewModel(
        bluetoothAdapterProvider: BluetoothAdapterProvider = enabledBluetoothAdapter
    ): SessionEstablishmentViewModel = SessionEstablishmentViewModel(
        bluetoothAdapterProvider = bluetoothAdapterProvider,
        verifierSessionFactory = { mdocVerifierSession },
        scanner = fakeBluetoothScanner,
        logger = logger,
        bluetoothStatusMonitor = FakeBluetoothStateMonitor(),
        savedStateHandle = SavedStateHandle()
    )

    @Before
    fun setup() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    private val mdocVerifierSession = FakeVerifierSession()
    private val fakeBluetoothScanner = FakeAndroidBluetoothScanner()

    @Test
    fun `opens system Bluetooth alert when the Bluetooth is disabled`() = runTest {
        testViewModel = createViewModel(bluetoothAdapterProvider = disabledBluetoothAdapter)
        composeTestRule.run {
            render(
                undecodableState,
                Modifier,
                testViewModel,
                bluetoothPermissionsGranted
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.assertBluetoothPromptIsDisplayed()
    }

    @Test
    fun `does not attempt to open system Bluetooth alert when Bluetooth is enabled`() = runTest {
        testViewModel = createViewModel()

        composeTestRule.run {
            render(
                undecodableState,
                Modifier,
                testViewModel,
                bluetoothPermissionsGranted
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.assertBluetoothPromptIsNotDisplayed()
    }

    @Test
    fun `does not attempt to open system Bluetooth alert when permissions are not granted`() =
        runTest {
            testViewModel = createViewModel()

            composeTestRule.run {
                render(
                    undecodableState,
                    Modifier,
                    testViewModel,
                    bluetoothPermissionsDenied
                )
            }

            composeTestRule.waitForIdle()
            composeTestRule.assertBluetoothPromptIsNotDisplayed()
        }

    @Test
    fun cannotDecodeProvidedCborString() = runTest {
        testViewModel = createViewModel(bluetoothAdapterProvider = disabledBluetoothAdapter)

        composeTestRule.run {
            render(
                undecodableState,
                Modifier,
                testViewModel,
                bluetoothPermissionsGranted
            )
            assertBasicInformationIsDisplayed()
            assertDecodingErrorIsDisplayed()
            assertDeviceEngagementDataDoesNotExist()
            assertBluetoothPermissionIsGranted()
            assertDeviceBluetoothIsDisabled()
            assertIsNotSearchingForBluetoothDevices()
        }
    }

    @Test
    fun validCborExistsOnScreen() = runTest {
        composeTestRule.run {
            testViewModel = createViewModel(bluetoothAdapterProvider = disabledBluetoothAdapter)
            render(
                decodableDeniedState,
                Modifier,
                testViewModel,
                bluetoothPermissionsGranted
            )
            assertBasicInformationIsDisplayed()
            assertDecodingErrorDoesNotExist()
            assertDeviceEngagementDataIsDisplayed()
            assertBluetoothPermissionIsGranted()
            assertDeviceBluetoothIsDisabled()
            assertIsNotSearchingForBluetoothDevices()
        }
    }

    @Test
    fun bluetoothPermissionIsGrantedButDeviceBluetoothIsDisabled() = runTest {
        testViewModel = createViewModel(bluetoothAdapterProvider = disabledBluetoothAdapter)

        val stateForTest = decodableGrantedState

        composeTestRule.waitForIdle()

        composeTestRule.run {
            render(stateForTest, Modifier, testViewModel, bluetoothPermissionsGranted)
            assertBasicInformationIsDisplayed()
            assertDecodingErrorDoesNotExist()
            assertDeviceEngagementDataIsDisplayed()
            assertBluetoothPermissionIsGranted()
            assertDeviceBluetoothIsDisabled()
            assertIsNotSearchingForBluetoothDevices()
        }
    }

    @Test
    fun bluetoothPermissionIsGranted() = runTest {
        testViewModel = createViewModel(bluetoothAdapterProvider = disabledBluetoothAdapter)
        composeTestRule.run {
            render(
                decodableGrantedState,
                Modifier,
                testViewModel,
                bluetoothPermissionsGranted
            )
            assertBasicInformationIsDisplayed()
            assertDecodingErrorDoesNotExist()
            assertDeviceEngagementDataIsDisplayed()
            assertBluetoothPermissionIsGranted()
            assertDeviceBluetoothIsDisabled()
            assertIsNotSearchingForBluetoothDevices()
        }
    }

    @Test
    fun grantedAndEnabledBluetoothWithValidCborStartsScanning() = runTest {
        testViewModel = createViewModel()

        composeTestRule.run {
            render(
                validWithCorrectBluetoothSetup,
                Modifier,
                testViewModel,
                bluetoothPermissionsGranted
            )
            assertBasicInformationIsDisplayed()
            assertDecodingErrorDoesNotExist()
            assertDeviceEngagementDataIsDisplayed()
            assertBluetoothPermissionIsGranted()
            assertDeviceBluetoothIsEnabled()
            assertIsSearchingForBluetoothDevices()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `LaunchedEffect collects navEvents and calls onConnectionError`() = runTest {
        testViewModel = createViewModel()

        composeTestRule.run {
            var receivedError: ConnectWithHolderDeviceError? = null

            render(
                genericErrorState,
                Modifier,
                testViewModel,
                bluetoothPermissionsGranted,
                onFindError = { receivedError = it }
            )

            mdocVerifierSession.updateState(VerifierSessionState.Error("test"))

            waitForIdle()

            assertEquals(
                ConnectWithHolderDeviceError.GenericError,
                receivedError
            )
        }
    }

    @Test
    fun connectWithHolderDevicePreviewRendersWithValidCbor() = runTest {
        composeTestRule.run {
            renderPreview(state = validWithCorrectBluetoothSetup)
            assertBasicInformationIsDisplayed()
        }
    }
}
