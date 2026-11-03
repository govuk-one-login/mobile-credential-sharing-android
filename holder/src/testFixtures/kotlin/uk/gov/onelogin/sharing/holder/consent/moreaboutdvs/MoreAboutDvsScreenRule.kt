package uk.gov.onelogin.sharing.holder.consent.moreaboutdvs

import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.test.core.app.ApplicationProvider
import uk.gov.android.ui.componentsv2.matchers.SemanticsMatchers.hasRole
import uk.gov.android.ui.theme.minimumTouchTarget
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
            "first" to R.string.more_about_dvs_body_1,
            "second" to R.string.more_about_dvs_body_2,
            "third" to R.string.more_about_dvs_body_3
        ).forEach { (description, resourceId) ->
            onNodeWithText(
                resources.getString(
                    resourceId,
                    organizationName
                ).removeDoubleFullStop(),
                useUnmergedTree = useUnmergedTree
            ).assertExists("Cannot find $description paragraph")
        }
    }

    fun assertValidBulletPoints(
        organizationName: String,
        useUnmergedTree: Boolean = false
    ) {
        mapOf(
            "first" to R.string.more_about_dvs_bullet_1,
            "second" to R.string.more_about_dvs_bullet_2,
            "third" to R.string.more_about_dvs_bullet_3
        ).forEach { (description, resourceId) ->
            onNodeWithText(
                resources.getString(
                    resourceId,
                    organizationName
                ).removeDoubleFullStop(),
                useUnmergedTree = useUnmergedTree
            ).assertExists("Cannot find $description bullet point")
        }
    }

    fun assertValidHeader(
        organizationName: String,
        useUnmergedTree: Boolean = false
    ) {
        onHeader(
            organizationName = organizationName,
            useUnmergedTree = useUnmergedTree
        ).assertExists("Cannot find the main header!")
            .assert(isHeading())
    }

    fun assertValidPrivacyPolicyBody(
        organizationName: String,
        useUnmergedTree: Boolean = false
    ) = onPrivacyPolicyBody(
        organizationName = organizationName,
        useUnmergedTree = useUnmergedTree
    ).assertExists("Cannot find the main privacy policy body!")

    fun assertValidPrivacyPolicyBulletPoints(
        useUnmergedTree: Boolean = false
    ) {
        mapOf(
            "first" to R.string.more_about_dvs_privacy_bullet_1,
            "second" to R.string.more_about_dvs_privacy_bullet_2,
        ).forEach { (description, resourceId) ->
            onContainer().performScrollToNode(
                hasText(
                    resources.getString(resourceId),
                )
            )
            onNodeWithText(
                resources.getString(resourceId),
                useUnmergedTree = useUnmergedTree,
            ).assertExists("Cannot find $description privacy policy bullet point")
        }
    }

    fun assertValidPrivacyPolicyButton(
        useUnmergedTree: Boolean = false
    ) {
        onPrivacyPolicyButton(useUnmergedTree)
            .assertExists()
            .assertHasClickAction()
            .assertIsEnabled()
            .assertIsDisplayed()
            .assertWidthIsAtLeast(minimumTouchTarget)
            .assertHeightIsAtLeast(minimumTouchTarget)
    }

    fun assertValidPrivacyPolicySubheading(
        organizationName: String,
        useUnmergedTree: Boolean = false
    ) = onPrivacyPolicySubheading(
        organizationName = organizationName,
        useUnmergedTree = useUnmergedTree
    ).assertExists("Cannot find the main privacy policy subheading!")
        .assert(isHeading())

    fun assertValidUi(expectedOrganization: String, useUnmergedTree: Boolean = false) {
        assertValidHeader(organizationName = expectedOrganization, useUnmergedTree)
        assertValidBody(organizationName = expectedOrganization, useUnmergedTree)
        assertValidBulletPoints(organizationName = expectedOrganization, useUnmergedTree)
        assertValidPrivacyPolicySubheading(organizationName = expectedOrganization, useUnmergedTree)
        assertValidPrivacyPolicyBody(organizationName = expectedOrganization, useUnmergedTree)
        assertValidPrivacyPolicyBulletPoints(useUnmergedTree = true)
        assertValidPrivacyPolicyButton(useUnmergedTree = true)
    }

    fun onContainer() = onNode(hasScrollAction())

    fun onPrivacyPolicyBody(
        organizationName: String,
        useUnmergedTree: Boolean = false
    ): SemanticsNodeInteraction {
        val body = resources.getString(
            R.string.more_about_dvs_privacy_body,
            organizationName
        )
        onContainer().performScrollToNode(
            hasText(body)
        )

        return onNodeWithText(
            body,
            useUnmergedTree = useUnmergedTree
        )
    }

    fun onPrivacyPolicySubheading(
        organizationName: String,
        useUnmergedTree: Boolean = false
    ): SemanticsNodeInteraction {
        val subHeading = resources.getString(
            R.string.more_about_dvs_privacy_subheading,
            organizationName
        )

        onContainer().performScrollToNode(
            hasText(subHeading)
        )

        return onNodeWithText(
            subHeading,
            useUnmergedTree = useUnmergedTree
        )
    }

    fun onPrivacyPolicyButton(
        useUnmergedTree: Boolean = false
    ): SemanticsNodeInteraction {
        onContainer().performScrollToNode(hasClickAction())

        return onNode(
            hasRole(Role.Button),
            useUnmergedTree = useUnmergedTree
        )
    }

    fun onHeader(
        organizationName: String,
        useUnmergedTree: Boolean = false
    ): SemanticsNodeInteraction {
        val heading = resources.getString(R.string.more_about_dvs_title, organizationName)

        return onNodeWithText(
            heading,
            useUnmergedTree = useUnmergedTree
        )
    }
}
