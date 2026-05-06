package uk.gov.onelogin.sharing.holder

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.testing.TestNavHostController
import androidx.test.annotation.UiThreadTest
import com.google.testing.junit.testparameterinjector.TestParameters
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.onelogin.sharing.holder.HolderRoutes.configureHolderRoutes
import uk.gov.onelogin.sharing.holder.prerequisites.HolderPrerequisitesViewModel
import uk.gov.onelogin.sharing.holder.presentation.HolderWelcomeScreenRule
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator

@RunWith(RobolectricTestParameterInjector::class)
class HolderRoutesTest {

    private lateinit var controller: TestNavHostController

    @get:Rule
    val composeTestRule = HolderWelcomeScreenRule(
        composeTestRule = createComposeRule()
    )

    @Test
    @UiThreadTest
    @TestParameters(valuesProvider = HolderRouteParameters::class)
    fun `Navigates to route`(route: Any, conversion: TestNavHostController.() -> Any?) = runTest {
        val viewModelFactory: MetroViewModelFactory = mockk(relaxed = true)
        every {
            viewModelFactory.create(
                HolderPrerequisitesViewModel::class,
                any()
            )
        } returns HolderPrerequisitesViewModel(
            orchestrator = FakeOrchestrator()
        )

        composeTestRule.setContent {
            val context = LocalContext.current
            controller = TestNavHostController(context).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }

            Render(viewModelFactory)
        }

        composeTestRule.waitForIdle()

        controller.navigate(route)

        composeTestRule.waitUntil {
            conversion(controller) != null
        }
    }

    @Composable
    private fun Render(viewModelFactory: MetroViewModelFactory) {
        CompositionLocalProvider(
            LocalMetroViewModelFactory provides viewModelFactory
        ) {
            NavHost(
                navController = controller,
                startDestination = HolderRoutes
            ) {
                configureHolderRoutes(controller)
            }
        }
    }
}
