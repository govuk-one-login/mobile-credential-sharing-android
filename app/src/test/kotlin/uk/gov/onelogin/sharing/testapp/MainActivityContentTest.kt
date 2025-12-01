package uk.gov.onelogin.sharing.testapp

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.testapp.destination.PrimaryTabDestination
import uk.gov.onelogin.sharing.testapp.destination.PrimaryTabDestinationData.expectedHolderMenuItems
import uk.gov.onelogin.sharing.testapp.destination.PrimaryTabDestinationData.expectedVerifierMenuItems

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
            assertMenuItemsCount(expectedHolderMenuItems.size)
            expectedHolderMenuItems.forEach(::assertMenuItem)
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
            assertMenuItemsCount(expectedVerifierMenuItems.size)
            expectedVerifierMenuItems.forEach(::assertMenuItem)
        }
    }

    @Test
    fun previewDisplaysHolderContent() = runTest {
        composeTestRule.run {
            renderPreview(
                currentTabDestination = PrimaryTabDestination.Holder
            )
        }
    }

    @Test
    fun previewDisplaysVerifierContent() = runTest {
        composeTestRule.run {
            renderPreview(
                currentTabDestination = PrimaryTabDestination.Verifier
            )
        }
    }
}
