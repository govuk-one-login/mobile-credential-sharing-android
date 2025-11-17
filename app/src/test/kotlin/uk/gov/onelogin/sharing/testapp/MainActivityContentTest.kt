package uk.gov.onelogin.sharing.testapp

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.testapp.destination.PrimaryTabDestination

@RunWith(AndroidJUnit4::class)
class MainActivityContentTest {

    @get:Rule
    val composeTestRule = MainActivityRule(createComposeRule())

    @Test
    fun displaysHolderMenuItems() = runTest {
        composeTestRule.run {
            render(
                currentTabDestination = PrimaryTabDestination.Holder,
                startDestination = PrimaryTabDestination.Holder
            )
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
            render(
                currentTabDestination = PrimaryTabDestination.Holder,
                startDestination = PrimaryTabDestination.Holder
            )
            performVerifierTabClick()
            val expectedMenuItems = listOf(
                "QR Scanner"
            )
            assertMenuItemsCount(expectedMenuItems.size)
            expectedMenuItems.forEach(composeTestRule::assertMenuItem)
        }
    }
}
