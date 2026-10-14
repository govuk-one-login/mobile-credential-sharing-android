package uk.gov.onelogin.sharing.holder.getreadytoshare

import androidx.compose.ui.test.junit4.v2.createComposeRule
import kotlinx.coroutines.test.runTest
import org.junit.Assume.assumeFalse
import org.junit.Rule
import kotlin.test.Test

class GetReadyToShareScreenScreenshotTest {

    @get:Rule
    val composeTestRule = GetReadyToShareScreenRule(createComposeRule())

    @Test
    fun `Matches screenshot baseline`() = runTest {
        assumeFalse("DCMAW-23663: Implement roborazzi screenshot test", false)
    }

}