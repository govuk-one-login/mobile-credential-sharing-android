package uk.gov.onelogin.sharing.verifier.scan.buttons

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.core.presentation.buttons.PermanentPermissionDenialButton
import uk.gov.onelogin.sharing.core.presentation.buttons.PermanentPermissionDenialButtonPreview
import uk.gov.onelogin.sharing.verifier.R
import uk.gov.onelogin.sharing.verifier.scan.VerifierScannerRule

@RunWith(AndroidJUnit4::class)
class PermanentPermissionDenialButtonTest {
    @get:Rule
    val composeTestRule = VerifierScannerRule(createComposeRule())

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun standardUsage() = verifyBehaviour {
        PermanentPermissionDenialButton(
            context = LocalContext.current,
            titleText = stringResource(
                R.string.verifier_scanner_camera_permission_permanently_denied
            ),
            buttonText = stringResource(
                R.string.verifier_scanner_require_open_permissions
            )
        )
    }

    @Test
    fun previewUsage() = verifyBehaviour {
        PermanentPermissionDenialButtonPreview()
    }

    private fun verifyBehaviour(content: @Composable () -> Unit = {}) = runTest {
        composeTestRule.run {
            setContent(content)
            performOpenAppSettingsClick()
        }
    }
}
