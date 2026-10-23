package uk.gov.onelogin.sharing.holder.presentation.component

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.Test
import org.junit.Rule
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.holder.presentation.HolderWelcomeScreenRule

@RunWith(AndroidJUnit4::class)
class HolderWelcomeBodyTest {

    @get:Rule
    val composeTestRule = HolderWelcomeScreenRule(createComposeRule())

    @Test
    fun `Displays body`() {
        composeTestRule.setContent { HolderWelcomeBody() }
        composeTestRule.assertBodyIsDisplayed()
    }
}
