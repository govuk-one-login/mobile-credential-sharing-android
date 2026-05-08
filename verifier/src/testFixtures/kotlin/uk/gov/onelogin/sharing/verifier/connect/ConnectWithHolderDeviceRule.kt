package uk.gov.onelogin.sharing.verifier.connect

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithText
import com.google.accompanist.permissions.ExperimentalPermissionsApi

@OptIn(ExperimentalPermissionsApi::class)
class ConnectWithHolderDeviceRule(composeContentTestRule: ComposeContentTestRule) :
    ComposeContentTestRule by composeContentTestRule {

    private lateinit var renderState: ConnectWithHolderDeviceState

    fun update(state: ConnectWithHolderDeviceState) {
        renderState = state
    }

    fun assertPlaceholderTextExists() = onNodeWithText("Connect with holder device screen")
        .assertExists()
}
