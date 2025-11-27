package uk.gov.onelogin.sharing.verifier.connect

import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import uk.gov.onelogin.sharing.verifier.R

class ConnectWithHolderDeviceRule(
    composeContentTestRule: ComposeContentTestRule,
    private val decodeError: String,
    private val header: String
) : ComposeContentTestRule by composeContentTestRule {

    constructor(
        composeContentTestRule: ComposeContentTestRule,
        resources: Resources = ApplicationProvider.getApplicationContext<Context>().resources
    ) : this (
        composeContentTestRule = composeContentTestRule,
        decodeError = resources.getString(R.string.connect_with_holder_error_decode),
        header = resources.getString(R.string.connect_with_holder_heading)
    )

    fun assertBasicInformationIsDisplayed(base64EncodedEngagement: String) {
        onNodeWithText(header)
            .assertExists()
            .assertIsDisplayed()

        onNodeWithText(base64EncodedEngagement)
            .assertExists()
            .assertIsDisplayed()
    }

    fun assertDeviceEngagementDataDoesNotExist() = onNodeWithText(
        "DeviceEngagementDto",
        substring = true
    ).assertDoesNotExist()

    fun assertDeviceEngagementDataIsDisplayed() = onNodeWithText(
        "DeviceEngagementDto",
        substring = true
    ).assertExists()
        .assertIsDisplayed()

    fun assertErrorDoesNotExist() = onNodeWithText(decodeError)
        .assertDoesNotExist()

    fun assertErrorIsDisplayed() = onNodeWithText(decodeError)
        .assertExists()
        .assertIsDisplayed()

    fun render(base64EncodedEngagement: String, modifier: Modifier = Modifier.Companion) {
        setContent {
            ConnectWithHolderDeviceScreen(
                base64EncodedEngagement = base64EncodedEngagement,
                modifier = modifier
            )
        }
    }
}
