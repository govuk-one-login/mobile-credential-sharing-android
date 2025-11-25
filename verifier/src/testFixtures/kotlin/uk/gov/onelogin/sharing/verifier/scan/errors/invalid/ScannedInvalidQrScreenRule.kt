package uk.gov.onelogin.sharing.verifier.scan.errors.invalid

import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.Modifier
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

/**
 * JUnit 4 Rule for encapsulating assertion / performance behaviour for the [ScannedInvalidQrScreen]
 * UI composable.
 */
class ScannedInvalidQrScreenRule(
    composeContentTestRule: ComposeContentTestRule,
    private val body: String,
    private val button: String,
    private val iconDescription: String,
    private val title: String
) : ComposeContentTestRule by composeContentTestRule {
    private var tryAgainButtonClickCount = 0

    constructor(
        composeContentTestRule: ComposeContentTestRule,
        resources: Resources =
            ApplicationProvider.getApplicationContext<Context>().resources
    ) : this(
        body = resources.getString(R.string.scanned_invalid_qr_body),
        button = resources.getString(R.string.scanned_invalid_qr_try_again),
        composeContentTestRule = composeContentTestRule,
        iconDescription = resources.getString(ErrorScreenIcon.ErrorIcon.description),
        title = resources.getString(R.string.scanned_invalid_qr_title)
    )

    fun assertBodyTextIsDisplayed() = onBodyText().assertIsDisplayed()

    fun assertErrorIconIsDisplayed() = onErrorIcon().assertIsDisplayed()

    fun assertTitleIsDisplayed() = onTitleText().assertIsDisplayed()

    fun assertTryAgainButtonIsDisplayed() = onTryAgainButton().assertIsDisplayed()

    fun assertTryAgainButtonWasClicked() = assertThat(
        tryAgainButtonClickCount,
        greaterThan(0)
    )

    fun onBodyText() = onNodeWithText(body).assertExists()

    fun onErrorIcon() = onNode(hasRole(Role.Image), useUnmergedTree = true)
        .assertExists()
        .assertContentDescriptionEquals(iconDescription)

    fun onTitleText() = onNodeWithText(title).assertExists()

    fun onTryAgainButton() = onNodeWithText(button)
        .assertExists()
        .assertHasClickAction()
        .assert(hasRole(Role.Button))

    fun performTryAgainButtonClick() = onTryAgainButton().performClick()

    fun render(inputUri: String, modifier: Modifier = Modifier, onTryAgainClick: () -> Unit = {}) {
        setContent {
            ScannedInvalidQrScreen(
                inputUri = inputUri,
                modifier = modifier,
                onTryAgainClick = {
                    tryAgainButtonClickCount++
                    onTryAgainClick()
                }
            )
        }
    }
}
