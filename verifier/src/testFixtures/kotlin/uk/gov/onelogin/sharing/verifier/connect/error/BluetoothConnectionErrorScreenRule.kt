package uk.gov.onelogin.sharing.verifier.connect.error

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
import uk.gov.onelogin.sharing.verifier.scan.errors.invalid.ScannedInvalidQrScreen

/**
 * JUnit 4 Rule for encapsulating assertion / performance behaviour for the [ScannedInvalidQrScreen]
 * UI composable.
 */
class BluetoothConnectionErrorScreenRule(
    composeContentTestRule: ComposeContentTestRule,
    private val button: String,
    private val iconDescription: String
) : ComposeContentTestRule by composeContentTestRule {
    private var tryAgainButtonClickCount = 0

    private var routeState: BluetoothConnectionErrorRoute? = null

    constructor(
        composeContentTestRule: ComposeContentTestRule,
        resources: Resources =
            ApplicationProvider.getApplicationContext<Context>().resources
    ) : this(
        button = resources.getString(R.string.bluetooth_connection_error_try_again),
        composeContentTestRule = composeContentTestRule,
        iconDescription = resources.getString(ErrorScreenIcon.ErrorIcon.description)
    )

    fun assertErrorIconIsDisplayed() = onErrorIcon().assertIsDisplayed()

    fun assertTitleIsDisplayed() = onTitleText().assertIsDisplayed()

    fun assertTryAgainButtonIsDisplayed() = onTryAgainButton().assertIsDisplayed()

    fun assertTryAgainButtonWasClicked() = assertThat(
        tryAgainButtonClickCount,
        greaterThan(0)
    )

    fun onErrorIcon() = onNode(hasRole(Role.Image), useUnmergedTree = true)
        .assertExists()
        .assertContentDescriptionEquals(iconDescription)

    fun onTitleText() = onNodeWithText(routeState!!.title).assertExists()

    fun onTryAgainButton() = onNodeWithText(button)
        .assertExists()
        .assertHasClickAction()
        .assert(hasRole(Role.Button))

    fun performTryAgainButtonClick() = onTryAgainButton().performClick()

    fun render(title: String, modifier: Modifier = Modifier, onTryAgainClick: () -> Unit = {}) {
        routeState = BluetoothConnectionErrorRoute(title = title)
        setContent {
            BluetoothConnectionErrorScreen(
                title = routeState!!.title,
                modifier = modifier,
                onTryAgainClick = {
                    tryAgainButtonClickCount++
                    onTryAgainClick()
                }
            )
        }
    }
}
