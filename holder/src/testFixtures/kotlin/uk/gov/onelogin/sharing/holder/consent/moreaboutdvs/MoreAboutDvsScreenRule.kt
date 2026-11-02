package uk.gov.onelogin.sharing.holder.consent.moreaboutdvs

import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import uk.gov.onelogin.sharing.holder.R

class MoreAboutDvsScreenRule(
    composeTestRule: ComposeContentTestRule = createComposeRule(),
    private val resources: Resources = ApplicationProvider.getApplicationContext<Context>()
        .resources,
) : ComposeContentTestRule by composeTestRule {

    fun assertValidBody(
        organizationName: String,
        useUnmergedTree: Boolean = false
    ) {
        mapOf(
            "first paragraph" to R.string.more_about_dvs_body_1,
            "second paragraph" to R.string.more_about_dvs_body_2,
            "third paragraph" to R.string.more_about_dvs_body_3
        ).forEach { (description, resourceId) ->
            onNodeWithText(
                resources.getString(resourceId, organizationName).let { text ->
                    if (text.endsWith("..")) {
                        text.dropLast(1)
                    } else {
                        text
                    }
                },
                useUnmergedTree = useUnmergedTree
            ).assertExists("Cannot find $description")
        }
    }

    fun assertValidHeader(
        organizationName: String,
        useUnmergedTree: Boolean = false
    ) = onHeader(
        organizationName = organizationName,
        useUnmergedTree = useUnmergedTree
    ).assertExists("Cannot find the main header!")
        .assert(isHeading())

    fun onHeader(
        organizationName: String,
        useUnmergedTree: Boolean = false
    ) = onNodeWithText(
        resources.getString(R.string.more_about_dvs_title, organizationName),
        useUnmergedTree = useUnmergedTree
    )
}
