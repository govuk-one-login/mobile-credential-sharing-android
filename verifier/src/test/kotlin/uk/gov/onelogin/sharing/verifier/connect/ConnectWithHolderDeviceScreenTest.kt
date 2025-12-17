package uk.gov.onelogin.sharing.verifier.connect

import android.Manifest
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionStatus
import com.google.testing.junit.testparameterinjector.TestParameter
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.api.adapter.FakeBluetoothAdapterProvider
import uk.gov.onelogin.sharing.bluetooth.api.scanner.FakeAndroidBluetoothScanner
import uk.gov.onelogin.sharing.core.presentation.permissions.FakeMultiplePermissionsState
import uk.gov.onelogin.sharing.core.presentation.permissions.FakePermissionState
import uk.gov.onelogin.sharing.security.DecoderStub.VALID_CBOR
import uk.gov.onelogin.sharing.security.DecoderStub.validDeviceEngagementDto
import uk.gov.onelogin.sharing.security.DeviceEngagementStub.ENGAGEMENT_EXPECTED_BASE_64
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceStateStubs.decodableDeniedState
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceStateStubs.decodableGrantedState
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceStateStubs.undecodableState
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceStateStubs.validWithCorrectBluetoothSetup

@OptIn(ExperimentalPermissionsApi::class)
@RunWith(RobolectricTestParameterInjector::class)
class ConnectWithHolderDeviceScreenTest {

    @get:Rule
    val composeTestRule = ConnectWithHolderDeviceRule(createComposeRule())

    @TestParameter(valuesProvider = ConnectWithHolderDeviceRenderProvider::class)
    lateinit var renderFunction: (
        ConnectWithHolderDeviceRule,
        ConnectWithHolderDeviceState,
        Modifier,
        SessionEstablishmentViewModel,
        MultiplePermissionsState
    ) -> Unit

    @Test
    fun cannotDecodeProvidedCborString() = runTest {
        val fakePermissionState = FakeMultiplePermissionsState(
            permissions = listOf(
                FakePermissionState(
                    permission = Manifest.permission.BLUETOOTH_CONNECT,
                    status = PermissionStatus.Denied(false)
                ),
                FakePermissionState(
                    permission = Manifest.permission.BLUETOOTH_ADVERTISE,
                    status = PermissionStatus.Denied(false)
                ),
                FakePermissionState(
                    permission = Manifest.permission.ACCESS_FINE_LOCATION,
                    status = PermissionStatus.Denied(false)
                )
            ),
            onLaunchPermission = {}
        )
        val fakeBluetoothProvider = FakeBluetoothAdapterProvider(isEnabled = false)
        val fakeBluetoothScanner = FakeAndroidBluetoothScanner()

        val testViewModel = SessionEstablishmentViewModel(
            bluetoothAdapterProvider = fakeBluetoothProvider,
            scanner = fakeBluetoothScanner,
            logger = SystemLogger()
        )

        composeTestRule.run {
            renderFunction(this, undecodableState, Modifier, testViewModel, fakePermissionState)
            assertBasicInformationIsDisplayed()
            assertErrorIsDisplayed()
            assertDeviceEngagementDataDoesNotExist()
            assertBluetoothPermissionIsDenied()
            assertDeviceBluetoothIsDisabled()
            assertIsNotSearchingForBluetoothDevices()
        }
    }

    @Test
    fun validCborExistsOnScreen() = runTest {
        val fakePermissionState = FakeMultiplePermissionsState(
            permissions = listOf(
                FakePermissionState(
                    permission = Manifest.permission.BLUETOOTH_CONNECT,
                    status = PermissionStatus.Denied(false)
                ),
                FakePermissionState(
                    permission = Manifest.permission.BLUETOOTH_ADVERTISE,
                    status = PermissionStatus.Denied(false)
                ),
                FakePermissionState(
                    permission = Manifest.permission.ACCESS_FINE_LOCATION,
                    status = PermissionStatus.Denied(false)
                )
            ),
            onLaunchPermission = {}
        )
        composeTestRule.run {
            val testViewModel = SessionEstablishmentViewModel(
                bluetoothAdapterProvider = FakeBluetoothAdapterProvider(isEnabled = false),
                scanner = FakeAndroidBluetoothScanner(),
                logger = SystemLogger()
            )
            renderFunction(this, decodableDeniedState, Modifier, testViewModel, fakePermissionState)
            assertBasicInformationIsDisplayed()
            assertErrorDoesNotExist()
            assertDeviceEngagementDataIsDisplayed()
            assertBluetoothPermissionIsDenied()
            assertDeviceBluetoothIsDisabled()
            assertIsNotSearchingForBluetoothDevices()
        }
    }

    @Test
    fun bluetoothPermissionIsGrantedButDeviceBluetoothIsDisabled() = runTest {
        val fakePermissionState = FakeMultiplePermissionsState(
            permissions = listOf(
                FakePermissionState(
                    permission = Manifest.permission.BLUETOOTH_CONNECT,
                    status = PermissionStatus.Granted
                ),
                FakePermissionState(
                    permission = Manifest.permission.BLUETOOTH_ADVERTISE,
                    status = PermissionStatus.Granted
                ),
                FakePermissionState(
                    permission = Manifest.permission.ACCESS_FINE_LOCATION,
                    status = PermissionStatus.Granted
                )
            ),
            onLaunchPermission = {}
        )
        val fakeBluetoothProvider = FakeBluetoothAdapterProvider(isEnabled = false)
        val fakeBluetoothScanner = FakeAndroidBluetoothScanner()

        val testViewModel = SessionEstablishmentViewModel(
            bluetoothAdapterProvider = fakeBluetoothProvider,
            scanner = fakeBluetoothScanner,
            logger = SystemLogger()
        )

        val stateForTest = decodableGrantedState

        composeTestRule.waitForIdle()

        composeTestRule.run {
            renderFunction(this, stateForTest, Modifier, testViewModel, fakePermissionState)
            assertBasicInformationIsDisplayed()
            assertErrorDoesNotExist()
            assertDeviceEngagementDataIsDisplayed()
            assertBluetoothPermissionIsGranted()
            assertDeviceBluetoothIsDisabled()
            assertIsNotSearchingForBluetoothDevices()
        }
    }

    @Test
    fun bluetoothPermissionIsGranted() = runTest {
        val fakePermissionState = FakeMultiplePermissionsState(
            permissions = listOf(
                FakePermissionState(
                    permission = Manifest.permission.BLUETOOTH_CONNECT,
                    status = PermissionStatus.Granted
                ),
                FakePermissionState(
                    permission = Manifest.permission.BLUETOOTH_ADVERTISE,
                    status = PermissionStatus.Granted
                ),
                FakePermissionState(
                    permission = Manifest.permission.ACCESS_FINE_LOCATION,
                    status = PermissionStatus.Granted
                )
            ),
            onLaunchPermission = {}
        )
        val fakeBluetoothProvider = FakeBluetoothAdapterProvider(isEnabled = false)
        val fakeBluetoothScanner = FakeAndroidBluetoothScanner()

        val testViewModel = SessionEstablishmentViewModel(
            bluetoothAdapterProvider = fakeBluetoothProvider,
            scanner = fakeBluetoothScanner,
            logger = SystemLogger()
        )
        composeTestRule.run {
            renderFunction(
                this,
                decodableGrantedState,
                Modifier,
                testViewModel,
                fakePermissionState
            )
            assertBasicInformationIsDisplayed()
            assertErrorDoesNotExist()
            assertDeviceEngagementDataIsDisplayed()
            assertBluetoothPermissionIsGranted()
            assertDeviceBluetoothIsDisabled()
            assertIsNotSearchingForBluetoothDevices()
        }
    }

    @Test
    fun grantedAndEnabledBluetoothWithValidCborStartsScanning() = runTest {
        val fakePermissionState = FakeMultiplePermissionsState(
            permissions = listOf(
                FakePermissionState(
                    permission = Manifest.permission.BLUETOOTH_CONNECT,
                    status = PermissionStatus.Granted
                ),
                FakePermissionState(
                    permission = Manifest.permission.BLUETOOTH_ADVERTISE,
                    status = PermissionStatus.Granted
                ),
                FakePermissionState(
                    permission = Manifest.permission.ACCESS_FINE_LOCATION,
                    status = PermissionStatus.Granted
                )
            ),
            onLaunchPermission = {}
        )
        val fakeBluetoothProvider = FakeBluetoothAdapterProvider(isEnabled = true)
        val fakeBluetoothScanner = FakeAndroidBluetoothScanner()

        val testViewModel = SessionEstablishmentViewModel(
            bluetoothAdapterProvider = fakeBluetoothProvider,
            scanner = fakeBluetoothScanner,
            logger = SystemLogger()
        )

        composeTestRule.run {
            renderFunction(
                this,
                validWithCorrectBluetoothSetup,
                Modifier,
                testViewModel,
                fakePermissionState
            )
            assertBasicInformationIsDisplayed()
            assertErrorDoesNotExist()
            assertDeviceEngagementDataIsDisplayed()
            assertBluetoothPermissionIsGranted()
            assertDeviceBluetoothIsEnabled()
            assertIsSearchingForBluetoothDevices()
        }
    }

    @Test
    fun shouldShowErrorScreenWhenShowErrorScreenSetTrue() {
        val errorState = ConnectWithHolderDeviceState(
            showErrorScreen = true,
            hasAllPermissions = true,
            isBluetoothEnabled = true
        )

        composeTestRule.setContent {
            ConnectWithHolderDeviceScreenContent(
                base64EncodedEngagement = ENGAGEMENT_EXPECTED_BASE_64,
                contentState = errorState,
                engagementData = validDeviceEngagementDto,
                permissionsGranted = true,
                modifier = Modifier
            )
        }

        composeTestRule.onNodeWithText("Generic error").isDisplayed()
    }

    @Test
    fun connectWithHolderDevicePreviewRendersWithValidCbor() {
        composeTestRule.setContent {
            ConnectWithHolderDevicePreview(
                base64EncodedEngagement = VALID_CBOR
            )
        }
        composeTestRule.onNodeWithText("Successfully scanned QR code data:").assertIsDisplayed()
    }
}
