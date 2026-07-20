package uk.gov.onelogin.sharing.holder

import android.content.Context
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onParent
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.DialogNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import androidx.test.annotation.UiThreadTest
import com.google.testing.junit.testparameterinjector.TestParameters
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.onelogin.sharing.holder.HolderRoutes.configureHolderRoutes

@RunWith(RobolectricTestParameterInjector::class)
class HolderRoutesTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val viewModelFactory: MetroViewModelFactory = mockk(relaxed = true)

    private lateinit var controller: TestNavHostController
    private lateinit var context: Context

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @UiThreadTest
    @TestParameters(valuesProvider = HolderRouteParameters::class)
    fun `Navigates to route`(route: Any, assertion: TestNavHostController.() -> Boolean) = runTest {
        composeTestRule.run {
            setContent {
                context = LocalContext.current
                controller = TestNavHostController(context).apply {
                    navigatorProvider.addNavigator(ComposeNavigator())
                    navigatorProvider.addNavigator(DialogNavigator())
                }

                Render(viewModelFactory) {
                    controller.navigate(route)
                }
            }

            waitForIdle()

            onNodeWithText("Navigate").onParent().performClick()

            advanceUntilIdle()

            waitUntil {
                assertion(controller)
            }
        }
    }

    @Composable
    private fun Render(viewModelFactory: MetroViewModelFactory, onClick: () -> Unit) {
        CompositionLocalProvider(
            LocalMetroViewModelFactory provides viewModelFactory
        ) {
            NavHost(
                navController = controller,
                startDestination = "unit test"
            ) {
                composable("unit test") {
                    Button(
                        onClick = onClick,
                    ) {
                        Text("Navigate")
                    }
                }
                configureHolderRoutes(controller)
            }
        }
    }
}
