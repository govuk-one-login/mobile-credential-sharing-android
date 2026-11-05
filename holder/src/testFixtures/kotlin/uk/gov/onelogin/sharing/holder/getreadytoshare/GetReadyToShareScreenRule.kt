package uk.gov.onelogin.sharing.holder.getreadytoshare

import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import uk.gov.android.ui.componentsv2.matchers.SemanticsMatchers.hasRole
import uk.gov.android.ui.theme.minimumTouchTarget
import uk.gov.onelogin.sharing.holder.R

class GetReadyToShareScreenRule(
    composeTestRule: ComposeContentTestRule = createComposeRule(),
    private val resources: Resources = ApplicationProvider
        .getApplicationContext<Context>().resources
) : ComposeContentTestRule by composeTestRule {

    fun assertValidAllowBluetoothButton(useUnmergedTree: Boolean = false) =
        onAllowActionButtons(useUnmergedTree)
            .onFirst()
            .assertExists()
            .assertHasClickAction()
            .assertIsEnabled()
            .assertIsDisplayed()
            .assertWidthIsAtLeast(minimumTouchTarget)
            .assertHeightIsAtLeast(minimumTouchTarget)

    fun assertValidBodyContents(useUnmergedTree: Boolean = false) {
        mapOf(
            "first paragraph" to R.string.get_ready_to_share_before_sharing,
            "second paragraph" to R.string.get_ready_to_share_if_bluetooth_is_off,
            "third paragraph" to R.string.get_ready_to_share_use_qr_to_connect
        ).forEach { (description, resourceId) ->
            onNodeWithText(resources.getString(resourceId), useUnmergedTree = useUnmergedTree)
                .assertExists("Cannot find $description")
        }
    }

    fun assertValidFindOutMoreButton(useUnmergedTree: Boolean = false) =
        onFindOutMoreButtons(useUnmergedTree)
            .onFirst()
            .assertExists()
            .assertHasClickAction()
            .assertIsEnabled()
            .assertIsDisplayed()
            .assertWidthIsAtLeast(minimumTouchTarget)
            .assertHeightIsAtLeast(minimumTouchTarget)

    fun assertValidHeader(useUnmergedTree: Boolean = false) = onHeader(useUnmergedTree)
        .assertExists()
        .assert(isHeading())

    fun onAllowActionButtons(useUnmergedTree: Boolean = false) = onAllNodes(
        hasRole(
            Role.Button
        ) and hasText(resources.getString(R.string.get_ready_to_share_allow_bluetooth)),
        useUnmergedTree = useUnmergedTree
    )

    fun onFindOutMoreButtons(useUnmergedTree: Boolean = false) = onAllNodes(
        hasRole(
            Role.Button
        ) and hasText(resources.getString(R.string.get_ready_to_share_find_out_more)),
        useUnmergedTree = useUnmergedTree
    )

    fun onHeader(useUnmergedTree: Boolean = false) = onNodeWithText(
        resources.getString(R.string.get_ready_to_share_header),
        useUnmergedTree = useUnmergedTree
    )

    fun performAllowActionClick(useUnmergedTree: Boolean = false) =
        onAllowActionButtons(useUnmergedTree)
            .onFirst()
            .performClick()

    fun performFindOutMoreClick(useUnmergedTree: Boolean = false) =
        onFindOutMoreButtons(useUnmergedTree)
            .onFirst()
            .performClick()
}
