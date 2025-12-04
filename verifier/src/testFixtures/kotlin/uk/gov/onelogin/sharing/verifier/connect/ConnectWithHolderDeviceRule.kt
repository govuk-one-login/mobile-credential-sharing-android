package uk.gov.onelogin.sharing.verifier.connect

import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import uk.gov.onelogin.sharing.bluetooth.R as bluetoothR
import uk.gov.onelogin.sharing.core.R as coreR
import uk.gov.onelogin.sharing.verifier.R

@OptIn(ExperimentalPermissionsApi::class)
class ConnectWithHolderDeviceRule(
    composeContentTestRule: ComposeContentTestRule,
    private val decodedDataHeader: String,
    private val decodeError: String,
    private val deniedBluetoothPermission: String,
    private val disabledDeviceBluetooth: String,
    private val enabledDeviceBluetooth: String,
    private val grantedBluetoothPermission: String,
    private val header: String
) : ComposeContentTestRule by composeContentTestRule {

    private lateinit var renderState: ConnectWithHolderDeviceState

    constructor(
        composeContentTestRule: ComposeContentTestRule,
        resources: Resources = ApplicationProvider.getApplicationContext<Context>().resources
    ) : this(
        composeContentTestRule = composeContentTestRule,
        decodedDataHeader = resources.getString(R.string.connect_with_holder_decoded_data),
        decodeError = resources.getString(R.string.connect_with_holder_error_decode),
        deniedBluetoothPermission = resources.getString(
            R.string.connect_with_holder_permission_state,
            resources.getString(coreR.string.denied)
        ),
        disabledDeviceBluetooth = resources.getString(
            R.string.connect_with_holder_bluetooth_state,
            resources.getString(coreR.string.disabled)
        ),
        enabledDeviceBluetooth = resources.getString(
            R.string.connect_with_holder_bluetooth_state,
            resources.getString(coreR.string.enabled)
        ),
        grantedBluetoothPermission = resources.getString(
            R.string.connect_with_holder_permission_state,
            resources.getString(coreR.string.granted)
        ),
        header = resources.getString(R.string.connect_with_holder_heading)
    )

    fun assertBasicInformationIsDisplayed() {
        onNodeWithText(header)
            .assertExists()
            .assertIsDisplayed()

        onNodeWithText(renderState.base64EncodedEngagement)
            .assertExists()
            .assertIsDisplayed()
    }

    fun assertBluetoothPermissionIsDenied() = onNodeWithText(deniedBluetoothPermission)
        .assertExists()
        .assertIsDisplayed()

    fun assertBluetoothPermissionIsGranted() = onNodeWithText(grantedBluetoothPermission)
        .assertExists()
        .assertIsDisplayed()

    fun assertDeviceBluetoothIsDisabled() {
        onNodeWithText(disabledDeviceBluetooth)
            .assertExists()
            .assertIsDisplayed()

        onNodeWithText(enabledDeviceBluetooth)
            .assertDoesNotExist()
    }

    fun assertDeviceBluetoothIsEnabled() {
        onNodeWithText(enabledDeviceBluetooth)
            .assertExists()
            .assertIsDisplayed()

        onNodeWithText(disabledDeviceBluetooth)
            .assertDoesNotExist()
    }

    fun assertDeviceEngagementDataDoesNotExist() {
        onNodeWithText(
            decodedDataHeader
        ).assertDoesNotExist()

        onNodeWithText(
            "DeviceEngagementDto",
            substring = true
        ).assertDoesNotExist()
    }

    fun assertDeviceEngagementDataIsDisplayed() {
        onNodeWithText(
            decodedDataHeader
        ).assertExists()
            .assertIsDisplayed()

        onNodeWithText(
            "DeviceEngagementDto",
            substring = true
        ).assertExists()
            .assertIsDisplayed()
    }

    fun assertErrorDoesNotExist() = onNodeWithText(decodeError)
        .assertDoesNotExist()

    fun assertErrorIsDisplayed() = onNodeWithText(decodeError)
        .assertExists()
        .assertIsDisplayed()

    fun render(state: ConnectWithHolderDeviceState, modifier: Modifier = Modifier) {
        update(state)
        setContent {
            ConnectWithHolderDeviceScreen(
                bluetoothAdapter = renderState.adapter,
                base64EncodedEngagement = renderState.base64EncodedEngagement,
                modifier = modifier,
                permissionState = renderState.permissionState
            )
        }
    }

    fun renderPreview(state: ConnectWithHolderDeviceState, modifier: Modifier = Modifier) {
        update(state)
        setContent {
            ConnectWithHolderDevicePreview(
                state = renderState,
                modifier = modifier
            )
        }
    }

    fun update(state: ConnectWithHolderDeviceState) {
        renderState = state
    }
}
