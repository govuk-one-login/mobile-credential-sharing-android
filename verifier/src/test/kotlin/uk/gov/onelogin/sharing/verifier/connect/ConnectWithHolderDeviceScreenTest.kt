package uk.gov.onelogin.sharing.verifier.connect

import android.Manifest
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionStatus
import com.google.testing.junit.testparameterinjector.TestParameter
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.onelogin.sharing.bluetooth.api.adapter.FakeBluetoothAdapterProvider
import uk.gov.onelogin.sharing.bluetooth.api.scanner.FakeAndroidBluetoothScanner
import uk.gov.onelogin.sharing.core.presentation.permissions.FakeMultiplePermissionsState
import uk.gov.onelogin.sharing.core.presentation.permissions.FakePermissionState
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
            scanner = fakeBluetoothScanner
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
                scanner = FakeAndroidBluetoothScanner()
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
    fun bluetoothPermissionIsGrantedButDeviceBluetoothIsDisabled() =
        runTest {
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
                scanner = fakeBluetoothScanner
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
            scanner = fakeBluetoothScanner
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
            scanner = fakeBluetoothScanner
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
}
