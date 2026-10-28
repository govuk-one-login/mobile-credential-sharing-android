package uk.gov.onelogin.sharing.holder

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.DialogNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.testing.TestNavHostController
import androidx.test.annotation.UiThreadTest
import com.google.testing.junit.testparameterinjector.TestParameters
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.onelogin.sharing.holder.HolderRoutes.configureHolderRoutes
import uk.gov.onelogin.sharing.holder.prerequisites.HolderPrerequisitesRoute
import uk.gov.onelogin.sharing.holder.presentation.HolderPresentQrRoute
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

@RunWith(RobolectricTestParameterInjector::class)
class MonitorHolderSessionStateTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val viewModelFactory: MetroViewModelFactory = mockk(relaxed = true)

    private lateinit var controller: TestNavHostController
    private lateinit var context: Context

    @Test
    @UiThreadTest
    @TestParameters(valuesProvider = HolderStateToNavigationRoute::class)
    fun `Session state converts to navigation route`(
        state: HolderSessionState,
        assertion: TestNavHostController.() -> Boolean
    ) = runTest {
        composeTestRule.run {
            setContent {
                context = LocalContext.current
                controller = TestNavHostController(context).apply {
                    navigatorProvider.addNavigator(ComposeNavigator())
                    navigatorProvider.addNavigator(DialogNavigator())
                }

                Render(viewModelFactory)
            }

            convertSessionStateToNavigation(
                context = context,
                navController = controller,
                state = state
            ).invoke()

            waitUntil {
                assertion(controller)
            }
        }
    }

    @Test
    @UiThreadTest
    fun `Complete Expired performs no navigation`() = runTest {
        composeTestRule.run {
            setContent {
                context = LocalContext.current
                controller = TestNavHostController(context).apply {
                    navigatorProvider.addNavigator(ComposeNavigator())
                    navigatorProvider.addNavigator(DialogNavigator())
                }

                Render(viewModelFactory)
            }

            val startRoute = controller.currentBackStackEntry?.destination?.route

            convertSessionStateToNavigation(
                context = context,
                navController = controller,
                state = HolderSessionState.Complete.Expired
            ).invoke()

            assert(controller.currentBackStackEntry?.destination?.route == startRoute)
        }
    }

    @Test
    @UiThreadTest
    fun `NotStarted during refresh stays on the QR screen`() = runTest {
        composeTestRule.run {
            setContent {
                context = LocalContext.current
                controller = TestNavHostController(context).apply {
                    navigatorProvider.addNavigator(ComposeNavigator())
                    navigatorProvider.addNavigator(DialogNavigator())
                }

                Render(viewModelFactory)
            }

            // Arrive on the QR screen (as during an active presentation / grace window).
            convertSessionStateToNavigation(
                context = context,
                navController = controller,
                state = HolderSessionState.PresentingEngagement("qr")
            ).invoke()
            waitUntil {
                controller.currentBackStackEntry?.destination
                    ?.route == HolderPresentQrRoute::class.qualifiedName
            }

            // Refresh emits a transient NotStarted for the new session. While already presenting,
            // this must NOT navigate away (e.g. to the Prerequisites screen).
            convertSessionStateToNavigation(
                context = context,
                navController = controller,
                state = HolderSessionState.NotStarted
            ).invoke()

            assert(
                controller.currentBackStackEntry?.destination
                    ?.route == HolderPresentQrRoute::class.qualifiedName
            )
        }
    }

    @Test
    @UiThreadTest
    fun `NotStarted when not presenting navigates to the Prerequisites screen`() = runTest {
        composeTestRule.run {
            setContent {
                context = LocalContext.current
                controller = TestNavHostController(context).apply {
                    navigatorProvider.addNavigator(ComposeNavigator())
                    navigatorProvider.addNavigator(DialogNavigator())
                }

                Render(viewModelFactory)
            }

            // From the journey start (not on the QR screen), NotStarted still routes to
            // Prerequisites as normal.
            convertSessionStateToNavigation(
                context = context,
                navController = controller,
                state = HolderSessionState.NotStarted
            ).invoke()

            waitUntil {
                controller.currentBackStackEntry?.destination
                    ?.route == HolderPrerequisitesRoute::class.qualifiedName
            }
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
                configureHolderRoutes()
            }
        }
    }
}
