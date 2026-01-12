package uk.gov.onelogin.sharing.verifier.connect

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.logging.testdouble.SystemLogger
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

@OptIn(ExperimentalPermissionsApi::class)
@RunWith(AndroidJUnit4::class)
class ConnectWithHolderDeviceScreenTest {

    @get:Rule
    val composeTestRule = ConnectWithHolderDeviceRule(createComposeRule())

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
        val testViewModel = SessionEstablishmentViewModel(
            bluetoothAdapterProvider = disabledBluetoothAdapter,
            scanner = fakeBluetoothScanner,
            logger = SystemLogger(),
            bluetoothStatusMonitor = FakeBluetoothStateMonitor(),
            verifierSessionFactory = { mdocVerifierSession }
        )

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
        val testViewModel = SessionEstablishmentViewModel(
            bluetoothAdapterProvider = enabledBluetoothAdapter,
            scanner = fakeBluetoothScanner,
            logger = SystemLogger(),
            bluetoothStatusMonitor = FakeBluetoothStateMonitor(),
            verifierSessionFactory = { mdocVerifierSession }
        )

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
            val testViewModel = SessionEstablishmentViewModel(
                bluetoothAdapterProvider = disabledBluetoothAdapter,
                scanner = fakeBluetoothScanner,
                logger = SystemLogger(),
                bluetoothStatusMonitor = FakeBluetoothStateMonitor(),
                verifierSessionFactory = { mdocVerifierSession }
            )

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
        val testViewModel = SessionEstablishmentViewModel(
            bluetoothAdapterProvider = disabledBluetoothAdapter,
            scanner = fakeBluetoothScanner,
            logger = SystemLogger(),
            bluetoothStatusMonitor = FakeBluetoothStateMonitor(),
            verifierSessionFactory = { mdocVerifierSession }
        )

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
            val testViewModel = SessionEstablishmentViewModel(
                bluetoothAdapterProvider = disabledBluetoothAdapter,
                scanner = fakeBluetoothScanner,
                logger = SystemLogger(),
                bluetoothStatusMonitor = FakeBluetoothStateMonitor(),
                verifierSessionFactory = { mdocVerifierSession }
            )
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
        val testViewModel = SessionEstablishmentViewModel(
            bluetoothAdapterProvider = disabledBluetoothAdapter,
            scanner = fakeBluetoothScanner,
            logger = SystemLogger(),
            bluetoothStatusMonitor = FakeBluetoothStateMonitor(),
            verifierSessionFactory = { mdocVerifierSession }
        )

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
        val testViewModel = SessionEstablishmentViewModel(
            bluetoothAdapterProvider = disabledBluetoothAdapter,
            scanner = fakeBluetoothScanner,
            logger = SystemLogger(),
            bluetoothStatusMonitor = FakeBluetoothStateMonitor(),
            verifierSessionFactory = { mdocVerifierSession }
        )
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
        val testViewModel = SessionEstablishmentViewModel(
            bluetoothAdapterProvider = enabledBluetoothAdapter,
            scanner = fakeBluetoothScanner,
            logger = SystemLogger(),
            bluetoothStatusMonitor = FakeBluetoothStateMonitor(),
            verifierSessionFactory = { mdocVerifierSession }
        )

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
    fun `Lambdas pass the error state via LaunchedEffect`() = runTest {
        val testViewModel = SessionEstablishmentViewModel(
            bluetoothAdapterProvider = enabledBluetoothAdapter,
            scanner = fakeBluetoothScanner,
            logger = SystemLogger(),
            bluetoothStatusMonitor = FakeBluetoothStateMonitor(),
            verifierSessionFactory = { mdocVerifierSession }
        ).also {
            it.updateState { genericErrorState }
        }

        composeTestRule.run {
            render(
                genericErrorState,
                Modifier,
                testViewModel,
                bluetoothPermissionsGranted
            )

            awaitIdle()

            assertErrorStateEquals(ConnectWithHolderDeviceError.GenericError)
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
