package uk.gov.onelogin.sharing.holder.getreadytoshare

import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.core.app.ApplicationProvider
import uk.gov.onelogin.sharing.holder.R

class GetReadyToShareScreenRule(
    composeTestRule: ComposeContentTestRule,
    private val resources: Resources = ApplicationProvider
        .getApplicationContext<Context>().resources
) : ComposeContentTestRule by composeTestRule {

    fun assertValidHeader() = onHeader()
        .assertExists()
        .assert(isHeading())
        .assertTextEquals(resources.getString(R.string.get_ready_to_share_header))

    fun onHeader() = onNodeWithTag("header")
}