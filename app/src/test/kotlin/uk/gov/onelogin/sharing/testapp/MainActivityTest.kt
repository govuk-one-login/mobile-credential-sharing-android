package uk.gov.onelogin.sharing.testapp

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val composeTestRule = MainActivityRule(createAndroidComposeRule<MainActivity>())

    @Test
    fun displaysHolderMenuItems() = runTest {
        composeTestRule.run {
            performHolderTabClick()
            val expectedMenuItems = listOf(
                "Welcome screen"
            )
            assertMenuItemsCount(expectedMenuItems.size)
            expectedMenuItems.forEach(composeTestRule::assertMenuItem)
        }
    }

    @Test
    fun displaysVerifierMenuItems() = runTest {
        composeTestRule.run {
            performVerifierTabClick()
            val expectedMenuItems = listOf(
                "QR Scanner"
            )
            assertMenuItemsCount(expectedMenuItems.size)
            expectedMenuItems.forEach(composeTestRule::assertMenuItem)
        }
    }
}
