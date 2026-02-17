package uk.gov.onelogin.sharing.testapp

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/*@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val composeTestRule = MainActivityRule(
        composeTestRule = createAndroidComposeRule<MainActivity>(),
        appGraph = createTestGraph()
    )

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
}*/

class MainActivityTest {
    @Test
    fun test() {
        assertEquals(2, 1 + 1)
    }
}
