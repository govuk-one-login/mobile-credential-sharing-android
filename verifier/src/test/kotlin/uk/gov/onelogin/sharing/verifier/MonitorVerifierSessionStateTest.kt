package uk.gov.onelogin.sharing.verifier

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.testing.TestNavHostController
import androidx.navigation.toRoute
import androidx.test.annotation.UiThreadTest
import com.google.testing.junit.testparameterinjector.TestParameters
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionStateStubs.preflightEmptyPermissions
import uk.gov.onelogin.sharing.verifier.VerifierRoutes.configureVerifierRoutes
import uk.gov.onelogin.sharing.verifier.verify.retry.RetryVerifierPrerequisitesRoute

@RunWith(RobolectricTestParameterInjector::class)
class MonitorVerifierSessionStateTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val viewModelFactory: MetroViewModelFactory = mockk(relaxed = true)

    private lateinit var controller: TestNavHostController
    private lateinit var context: Context

    @Test
    @UiThreadTest
    @TestParameters(valuesProvider = VerifierStateToNavigationRoute::class)
    fun `Session state converts to navigation route`(
        state: VerifierSessionState,
        assertion: TestNavHostController.() -> Boolean
    ) = runTest {
        composeTestRule.run {
            setContent {
                context = LocalContext.current
                controller = TestNavHostController(context).apply {
                    navigatorProvider.addNavigator(ComposeNavigator())
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @UiThreadTest
    fun `Monitoring state changes the navigation backstack entry`() = runTest {
        val stateFlow = MutableStateFlow<VerifierSessionState>(VerifierSessionState.NotStarted)
        composeTestRule.run {
            setContent {
                context = LocalContext.current
                controller = TestNavHostController(context).apply {
                    navigatorProvider.addNavigator(ComposeNavigator())
                }

                MonitorVerifierSessionState(
                    sessionState = stateFlow,
                    controller = controller,
                    dispatcher = this@runTest.testScheduler
                )
                Render(viewModelFactory)
            }

            stateFlow.emit(preflightEmptyPermissions)

            advanceUntilIdle()

            waitUntil {
                controller.currentBackStackEntry?.toRoute<RetryVerifierPrerequisitesRoute>() != null
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
                startDestination = VerifierRoutes
            ) {
                configureVerifierRoutes(controller)
            }
        }
    }
}
