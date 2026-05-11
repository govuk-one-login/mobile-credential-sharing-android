package uk.gov.onelogin.sharing.verifier.connect

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.core.MainDispatcherRule

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class ConnectWithHolderDeviceScreenTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = ConnectWithHolderDeviceRule(createComposeRule())

    @Test
    fun `Has placeholder text for session states other than connecting`() = runTest(
        dispatcherRule.testDispatcher
    ) {
        composeTestRule.run {
            setContent { Render() }

            assertPlaceholderTextExists()
        }
    }

    @Composable
    private fun Render() {
        ConnectWithHolderDeviceScreen()
    }
}
