package uk.gov.onelogin.sharing.verifier.connect

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithText
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import org.hamcrest.Matcher
import uk.gov.onelogin.sharing.core.presentation.bluetooth.BluetoothSessionError

@OptIn(ExperimentalPermissionsApi::class)
class ConnectWithHolderDeviceRule(composeContentTestRule: ComposeContentTestRule) :
    ComposeContentTestRule by composeContentTestRule {

    private lateinit var renderState: ConnectWithHolderDeviceState

    private var bluetoothSessionError: BluetoothSessionError? = null

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

    fun assertBluetoothSessionError(matcher: Matcher<in BluetoothSessionError>) =
        waitUntil { matcher.matches(this.bluetoothSessionError) }

    fun assertPlaceholderTextDoesNotExist() = onNodeWithText("Connect with holder device screen")
        .assertDoesNotExist()

    fun assertPlaceholderTextExists() = onNodeWithText("Connect with holder device screen")
        .assertExists()

    fun updateOnConnectionError(error: BluetoothSessionError) {
        this.bluetoothSessionError = error
    }
}
