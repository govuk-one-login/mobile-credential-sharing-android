package uk.gov.onelogin.sharing.holder.error.unfulfillable

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.runner.RunWith
import uk.gov.android.ui.theme.m3.GdsTheme
import kotlin.test.Test

@RunWith(AndroidJUnit4::class)
class UnfulfillableErrorScreenTest {
    @get:Rule
    val composeTestRule = UnfulfillableErrorScreenRule()

    @Test
    fun `Has valid UI elements`() = runTest {
        composeTestRule.run {
            setContent {
                Render()
            }

            assertValidUi()
        }
    }

    @Composable
    private fun Render() {
        GdsTheme {
            UnfulfillableErrorScreen(
                modifier = Modifier
                    .fillMaxSize()
            )
        }
    }
}