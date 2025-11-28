package uk.gov.onelogin.sharing.testapp

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.testapp.destination.PrimaryTabDestinationData.expectedHolderMenuItems
import uk.gov.onelogin.sharing.testapp.destination.PrimaryTabDestinationData.expectedVerifierMenuItems

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val composeTestRule = MainActivityRule(createAndroidComposeRule<MainActivity>())

    @Test
    fun displaysHolderMenuItems() = runTest {
        composeTestRule.run {
            performHolderTabClick()
            assertMenuItemsCount(expectedHolderMenuItems.size)
            expectedHolderMenuItems.forEach(::assertMenuItem)
        }
    }

    @Test
    fun displaysVerifierMenuItems() = runTest {
        composeTestRule.run {
            performVerifierTabClick()
            assertMenuItemsCount(expectedVerifierMenuItems.size)
            expectedVerifierMenuItems.forEach(::assertMenuItem)
        }
    }
}
