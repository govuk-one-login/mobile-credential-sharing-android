package uk.gov.onelogin.sharing.holder.awaitingresolution

import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import uk.gov.android.ui.componentsv2.matchers.SemanticsMatchers.hasRole
import uk.gov.onelogin.sharing.holder.R

class AwaitingVerifierResolutionScreenRule(
    private val composeTestRule: ComposeContentTestRule = createComposeRule()
) : ComposeContentTestRule by composeTestRule {

    private val resources: Resources by lazy {
        ApplicationProvider.getApplicationContext<Context>().resources
    }

    // node finders
    fun onSuccessIcon() = onNodeWithTag(SUCCESS_ICON_TEST_TAG, useUnmergedTree = true)
    fun onTitle() = onNode(
        hasText(resources.getString(R.string.holder_awaiting_verifier_resolution_title))
            and isHeading()
    )

    fun onBody() = onNodeWithText(
        resources.getString(R.string.person_will_check_details_shared)
    )

    fun onBodyFirstParagraph() = onNodeWithText(
        resources.getString(R.string.holder_awaiting_verifier_resolution_body_1)
    )

    fun onBodySecondParagraph() = onNodeWithText(
        resources.getString(R.string.holder_awaiting_verifier_resolution_body_2)
    )

    fun onGoBackToDocumentsButton() = onNode(
        hasText(resources.getString(R.string.go_back_to_documents)) and hasRole(Role.Button)
    )

    // assertions
    fun assertSuccessIconIsDisplayed() = onSuccessIcon().assertIsDisplayed()

    fun assertTitleIsDisplayed() = onTitle().assertIsDisplayed()

    fun assertWelshTitleIsDisplayed() =
        onNode(hasText("Manylion wedi’u rhannu") and isHeading()).assertIsDisplayed()

    fun assertBodyIsDisplayed() {
        onBodyFirstParagraph().assertIsDisplayed()
        onBodySecondParagraph().assertIsDisplayed()
    }

    fun assertGoBackToDocumentsButtonIsDisplayed() = onGoBackToDocumentsButton()
        .assertHasClickAction()
        .assertIsDisplayed()

    fun performGoBackToDocumentsClick() = onGoBackToDocumentsButton().performClick()
}
