package uk.gov.onelogin.sharing.verifier.scan.buttons

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.verifier.scan.VerifierScannerRule

@RunWith(AndroidJUnit4::class)
class PermanentCameraDenialTest {
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
        PermanentCameraDenial(
            context = LocalContext.current
        )
    }

    @Test
    fun previewUsage() = verifyBehaviour {
        PermanentCameraDenialPreview()
    }

    private fun verifyBehaviour(content: @Composable () -> Unit = {}) = runTest {
        composeTestRule.run {
            setContent(content)
            performOpenAppSettingsClick()
        }
    }
}
