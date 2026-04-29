package uk.gov.onelogin.sharing.verifier.connect

import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import uk.gov.onelogin.sharing.bluetooth.EnableBluetoothPromptRule
import uk.gov.onelogin.sharing.core.presentation.bluetooth.BluetoothSessionError
import uk.gov.onelogin.sharing.verifier.R
import uk.gov.onelogin.sharing.core.R as coreR

@OptIn(ExperimentalPermissionsApi::class)
class ConnectWithHolderDeviceRule(
    composeContentTestRule: ComposeContentTestRule,
    private val disabledDeviceBluetooth: String,
    private val enabledDeviceBluetooth: String,
) : ComposeContentTestRule by composeContentTestRule {

    private lateinit var renderState: ConnectWithHolderDeviceState

    constructor(
        composeContentTestRule: ComposeContentTestRule,
        resources: Resources = ApplicationProvider.getApplicationContext<Context>().resources
    ) : this(
        composeContentTestRule = composeContentTestRule,
        disabledDeviceBluetooth = resources.getString(
            R.string.connect_with_holder_bluetooth_state,
            resources.getString(coreR.string.disabled)
        ),
        enabledDeviceBluetooth = resources.getString(
            R.string.connect_with_holder_bluetooth_state,
            resources.getString(coreR.string.enabled)
        ),
    )

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
        onFindError: (BluetoothSessionError) -> Unit = {}
    ) {
        update(state)
        setContent {
            ConnectWithHolderDeviceScreen(
                modifier = modifier,
                viewModel = viewModel,
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
