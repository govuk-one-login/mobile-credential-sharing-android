package uk.gov.onelogin.sharing.holder.awaitingresolution

import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
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

    fun assertSuccessIconIsDisplayed() = onNodeWithContentDescription("Success")
        .assertIsDisplayed()

    fun assertTitleIsDisplayed() = onNodeWithText(
        resources.getString(R.string.details_shared)
    ).assertIsDisplayed()

    fun assertBodyIsDisplayed() {
        onNodeWithText(resources.getString(R.string.person_will_check_details_shared)).assertIsDisplayed()
        onNodeWithText(resources.getString(R.string.bluetooth_connection_will_end)).assertIsDisplayed()
    }

    fun assertGoBackToDocumentsButtonIsDisplayed() {
        onNode(
            hasText(resources.getString(R.string.go_back_to_documents)) and
                hasRole(Role.Button)
        ).assertIsDisplayed()
    }

    fun performGoBackToDocumentsClick() = onNode(
        hasText(resources.getString(R.string.go_back_to_documents)) and
            hasRole(Role.Button)
    ).performClick()
}
