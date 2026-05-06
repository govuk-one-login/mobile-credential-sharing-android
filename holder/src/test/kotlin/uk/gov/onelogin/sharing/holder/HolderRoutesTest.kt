package uk.gov.onelogin.sharing.holder

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.testing.TestNavHostController
import androidx.savedstate.SavedState
import androidx.test.annotation.UiThreadTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.endsWith
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.collection.IsCollectionWithSize.hasSize
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.holder.HolderRoutes.configureHolderRoutes
import uk.gov.onelogin.sharing.holder.prerequisites.HolderPrerequisitesRoute
import uk.gov.onelogin.sharing.holder.prerequisites.HolderPrerequisitesViewModel
import uk.gov.onelogin.sharing.holder.presentation.HolderWelcomeScreenRule
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator

@RunWith(AndroidJUnit4::class)
class HolderRoutesTest {

    private lateinit var controller: NavHostController

    @get:Rule
    val composeTestRule = HolderWelcomeScreenRule(
        composeTestRule = createComposeRule()
    )

    @Test
    @UiThreadTest
    fun `Configures routes for holder journey`() = runTest {
        val destinations = mutableListOf<Pair<NavDestination, SavedState?>>()
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

            controller.addOnDestinationChangedListener { _, destination, arguments ->
                destinations.add(destination to arguments)
            }

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

        composeTestRule.waitForIdle()

        assertThat(
            destinations,
            hasSize(1)
        )
        assertThat(
            destinations[0].first.route,
            endsWith(HolderPrerequisitesRoute.toString())
        )
    }
}
