package uk.gov.onelogin.sharing.holder.presentation.component

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.holder.presentation.HolderWelcomeScreenRule

@RunWith(AndroidJUnit4::class)
class HolderWelcomeHeadingTest {

    @get:Rule
    val composeTestRule = HolderWelcomeScreenRule(createComposeRule())

    @Test
    fun `Displays heading`() {
        composeTestRule.setContent { HolderWelcomeHeading() }
        composeTestRule.assertHeadingIsDisplayed()
    }
}
