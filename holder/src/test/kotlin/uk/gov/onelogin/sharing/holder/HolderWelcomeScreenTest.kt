package uk.gov.onelogin.sharing.holder

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.verifier.HolderWelcomeScreenRule

@RunWith(AndroidJUnit4::class)
class HolderWelcomeScreenTest {

    @get:Rule
    val composeTestRule = HolderWelcomeScreenRule(createComposeRule())

    @Test
    fun displaysWelcomeText() = runTest {
        composeTestRule.apply {
            render()
            assertWelcomeTextIsDisplayed()
        }
    }

    @Test
    fun displaysQrCode() = runTest {
        composeTestRule.apply {
            render()
            assertQrCodeIsDisplayed()
        }
    }
}
