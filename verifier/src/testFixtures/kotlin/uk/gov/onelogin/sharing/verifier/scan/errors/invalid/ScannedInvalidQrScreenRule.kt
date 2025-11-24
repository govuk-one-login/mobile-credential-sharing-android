package uk.gov.onelogin.sharing.verifier.scan.errors.invalid

import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.greaterThan
import uk.gov.android.ui.componentsv2.matchers.SemanticsMatchers.hasRole
import uk.gov.android.ui.patterns.errorscreen.v2.ErrorScreenIcon
import uk.gov.onelogin.sharing.verifier.R

class ScannedInvalidQrScreenRule(
    composeContentTestRule: ComposeContentTestRule,
    private val resources: Resources =
        ApplicationProvider.getApplicationContext<Context>().resources,
    private val testTag: String = "errorScreen"
) : ComposeContentTestRule by composeContentTestRule {
    private var tryAgainButtonClickCount = 0
    fun assertBodyTextIsDisplayed() = onBodyText().assertIsDisplayed()

    fun assertErrorIconIsDisplayed() = onErrorIcon().assertIsDisplayed()

    fun assertTitleIsDisplayed() = onTitleText().assertIsDisplayed()

    fun assertTryAgainButtonIsDisplayed() = onTryAgainButton().assertIsDisplayed()

    fun assertTryAgainButtonWasClicked() {
        assertThat(
            tryAgainButtonClickCount,
            greaterThan(0)
        )
    }

    fun onBodyText() = onNodeWithText(resources.getString(R.string.scanned_invalid_qr_body))
        .assertExists()

    fun onErrorIcon() = onNode(hasRole(Role.Image), useUnmergedTree = true)
        .assertExists()
        .assertContentDescriptionEquals(
            resources.getString(ErrorScreenIcon.ErrorIcon.description)
        )

    fun onTitleText() = onNodeWithText(resources.getString(R.string.scanned_invalid_qr_title))
        .assertExists()

    fun onTryAgainButton() = onNodeWithText(
        resources.getString(R.string.scanned_invalid_qr_try_again)
    )
        .assertExists()
        .assertHasClickAction()
        .assert(hasRole(Role.Button))

    fun performTryAgainButtonClick() = onTryAgainButton().performClick()

    fun render(inputUri: String, onTryAgainClick: () -> Unit = {}) {
        setContent {
            ScannedInvalidQrScreen(
                inputUri = inputUri,
                modifier = Modifier.testTag(testTag),
                onTryAgainClick = {
                    tryAgainButtonClickCount++
                    onTryAgainClick()
                }
            )
        }
    }
}
