package uk.gov.onelogin.sharing.testapp

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.zacsweers.metro.createGraphFactory
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.di.CredentialSharingAppGraph

@RunWith(AndroidJUnit4::class)
class MainActivityContentTest {

    val appGraph = createGraphFactory<CredentialSharingAppGraph.Factory>()
        .create(
            applicationContext = ApplicationProvider.getApplicationContext(),
            logger = SystemLogger()
        )

    @get:Rule
    val composeTestRule = MainActivityRule(
        composeTestRule = createComposeRule(),
        appGraph = appGraph
    )

    @Test
    fun `test set content`() {
        composeTestRule.render()

        composeTestRule.onNodeWithText("Holder").assertExists()
    }
}
