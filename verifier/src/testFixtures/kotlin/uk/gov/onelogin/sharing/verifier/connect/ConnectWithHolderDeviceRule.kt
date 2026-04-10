package uk.gov.onelogin.sharing.verifier.connect

import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import uk.gov.onelogin.sharing.bluetooth.EnableBluetoothPromptRule
import uk.gov.onelogin.sharing.core.R as coreR
import uk.gov.onelogin.sharing.core.presentation.bluetooth.BluetoothSessionError
import uk.gov.onelogin.sharing.verifier.R

@OptIn(ExperimentalPermissionsApi::class)
class ConnectWithHolderDeviceRule(
    composeContentTestRule: ComposeContentTestRule,
    private val deniedBluetoothPermission: String,
    private val disabledDeviceBluetooth: String,
    private val enabledDeviceBluetooth: String,
    private val grantedBluetoothPermission: String
) : ComposeContentTestRule by composeContentTestRule {

    private lateinit var renderState: ConnectWithHolderDeviceState

    constructor(
        composeContentTestRule: ComposeContentTestRule,
        resources: Resources = ApplicationProvider.getApplicationContext<Context>().resources
    ) : this(
        composeContentTestRule = composeContentTestRule,
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
        )
    )

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

    fun assertBluetoothPromptIsDisplayed() = EnableBluetoothPromptRule(this)
        .assertIsDisplayed()

    fun assertBluetoothPromptIsNotDisplayed() = EnableBluetoothPromptRule(this)
        .assertIsNotDisplayed()

    fun assertDeviceBluetoothIsEnabled() {
        onNodeWithText(enabledDeviceBluetooth)
            .assertExists()
            .assertIsDisplayed()

        onNodeWithText(disabledDeviceBluetooth)
            .assertDoesNotExist()
    }

    fun render(
        state: ConnectWithHolderDeviceState,
        modifier: Modifier = Modifier,
        viewModel: SessionEstablishmentViewModel,
        permissionsState: MultiplePermissionsState,
        onFindError: (BluetoothSessionError) -> Unit = {}
    ) {
        update(state)
        setContent {
            ConnectWithHolderDeviceScreen(
                modifier = modifier,
                viewModel = viewModel,
                multiplePermissionsState = permissionsState,
                onConnectionError = {
                    onFindError(it)
                }
            )
        }
    }

    fun update(state: ConnectWithHolderDeviceState) {
        renderState = state
    }
}
