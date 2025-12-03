package uk.gov.onelogin.sharing.verifier.connect

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.testing.junit.testparameterinjector.TestParameter
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceStateStubs.decodableDeniedState
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceStateStubs.decodableGrantedState
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceStateStubs.undecodableState

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
        }
    }

    @Test
    fun bluetoothPermissionIsGranted() = runTest {
        composeTestRule.run {
            renderFunction(decodableGrantedState, Modifier)
            assertBluetoothPermissionIsGranted()
        }
    }
}
