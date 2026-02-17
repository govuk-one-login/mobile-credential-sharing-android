package uk.gov.onelogin.sharing.ui.impl

import android.content.Context
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.navigation.toRoute
import androidx.test.core.app.ApplicationProvider
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import kotlin.reflect.KClass
import org.junit.Assert.assertNotNull
import uk.gov.onelogin.sharing.ui.api.CredentialSharingDestination
import uk.gov.onelogin.sharing.ui.impl.di.CredentialSharingUiGraph

class CredentialSharingUiNavHostRule(
    private val composeTestRule: ComposeContentTestRule,
    private val uiGraph: CredentialSharingUiGraph
) : ComposeContentTestRule by composeTestRule {
    private lateinit var controller: TestNavHostController
    private val context: Context = ApplicationProvider.getApplicationContext()

    fun render(startDestination: CredentialSharingDestination, modifier: Modifier = Modifier) {
        setContent {
            controller = TestNavHostController(context)
            controller.navigatorProvider.addNavigator(ComposeNavigator())

            CompositionLocalProvider(
                LocalMetroViewModelFactory provides uiGraph.metroViewModelFactory
            ) {
                CredentialSharingUiNavHost(
                    startDestination = startDestination,
                    navController = controller,
                    modifier = modifier.testTag("credentialSharingUiNavHost")
                )
            }
        }

        composeTestRule.waitForIdle()
    }

    fun <T : CredentialSharingDestination> assertCurrentRoute(klass: KClass<T>): T {
        val result = controller.currentBackStackEntry?.toRoute<T>(klass)
        assertNotNull(result)

        return result!!
    }
}
