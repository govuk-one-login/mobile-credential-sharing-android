package uk.gov.onelogin.sharing.verifier.connect

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.testing.junit.testparameterinjector.TestParameter
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
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
    lateinit var renderFunction: ConnectWithHolderDeviceRule.(
        ConnectWithHolderDeviceState,
        Modifier
    ) -> Unit

    @Test
    fun cannotDecodeProvidedCborString() = runTest {
        composeTestRule.run {
            renderFunction(undecodableState, Modifier)
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
        composeTestRule.run {
            renderFunction(decodableDeniedState, Modifier)
            assertBasicInformationIsDisplayed()
            assertErrorDoesNotExist()
            assertDeviceEngagementDataIsDisplayed()
            assertBluetoothPermissionIsDenied()
            assertDeviceBluetoothIsDisabled()
            assertIsNotSearchingForBluetoothDevices()
        }
    }

    @Test
    fun bluetoothPermissionIsGranted() = runTest {
        composeTestRule.run {
            renderFunction(decodableGrantedState, Modifier)
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
        composeTestRule.run {
            renderFunction(validWithCorrectBluetoothSetup, Modifier)
            assertBasicInformationIsDisplayed()
            assertErrorDoesNotExist()
            assertDeviceEngagementDataIsDisplayed()
            assertBluetoothPermissionIsGranted()
            assertDeviceBluetoothIsEnabled()
            assertIsSearchingForBluetoothDevices()
        }
    }

    @Test
    fun bluetoothPermissionRequestOccursViaLaunchEffect() = runTest {
        composeTestRule.run {
            var hasLaunchedPermission = false
            renderFunction(
                decodableDeniedState.copy(
                    permissionState = FakePermissionState.bluetoothConnect(
                        status = PermissionStatus.Denied(shouldShowRationale = false)
                    ) {
                        hasLaunchedPermission = true
                    }
                ),
                Modifier
            )

            testScheduler.advanceUntilIdle()

            assertTrue(hasLaunchedPermission)
        }
    }
}
