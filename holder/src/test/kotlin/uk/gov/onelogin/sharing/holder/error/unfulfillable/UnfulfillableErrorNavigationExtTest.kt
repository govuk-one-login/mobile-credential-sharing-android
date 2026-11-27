package uk.gov.onelogin.sharing.holder.error.unfulfillable

import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.performClick
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import androidx.navigation.toRoute
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.namedTestValues
import com.google.testing.junit.testparameterinjector.TestParameter
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import io.mockk.mockk
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.android.ui.componentsv2.rules.ComposeContentTestRuleExtensions.onNodeWithRole
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.onelogin.sharing.holder.HolderRoutes
import uk.gov.onelogin.sharing.holder.TestViewModelFactory
import uk.gov.onelogin.sharing.holder.error.ErrorScreenTemplateViewModel
import uk.gov.onelogin.sharing.holder.error.unfulfillable.UnfulfillableErrorNavigationExt.configureUnfulfillableErrorRoute
import uk.gov.onelogin.sharing.holder.error.unfulfillable.UnfulfillableErrorNavigationExt.navigateToUnfulfillableError
import uk.gov.onelogin.sharing.holder.navigation.TestNavHostControllerHelper.prepareController
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

@RunWith(RobolectricTestParameterInjector::class)
class UnfulfillableErrorNavigationExtTest {

    @get:Rule
    val composeTestRule = UnfulfillableErrorScreenRule()

    private lateinit var controller: TestNavHostController
    private var onBackPressedDispatcherOwner: OnBackPressedDispatcherOwner? = null

    private val initialHolderSessionState: HolderSessionState = HolderSessionState.Complete.Success(
        HolderSessionState.Complete.SuccessReason.UnfulfillableRequest
    )

    private val orchestrator by lazy {
        FakeOrchestrator(
            initialHolderState = MutableStateFlow(initialHolderSessionState)
        )
    }

    private val unfulfillableViewModel by lazy {
        UnfulfillableErrorViewModel(
            analyticsLogger = mockk(relaxed = true),
            orchestrator = orchestrator
        )
    }

    private val errorTemplateViewModel by lazy {
        ErrorScreenTemplateViewModel(
            analyticsLogger = mockk(relaxed = true)
        )
    }

    private val viewModelFactory: MetroViewModelFactory by lazy {
        TestViewModelFactory(
            viewModelProviders = mapOf(
                UnfulfillableErrorViewModel::class to { unfulfillableViewModel },
                ErrorScreenTemplateViewModel::class to { errorTemplateViewModel }
            )
        )
    }

    @Test
    fun `Correctly configures unfulfillable error route`() = runTest {
        composeTestRule.run {
            setContent {
                controller = prepareController(LocalContext.current)
                onBackPressedDispatcherOwner = LocalOnBackPressedDispatcherOwner.current
                Render()
            }

            repeat(2) {
                performButtonClick()
            }

            waitUntil {
                UnfulfillableErrorRoute == controller.currentBackStackEntry
                    ?.toRoute<UnfulfillableErrorRoute>()
            }
        }
    }

    @Test
    fun `User interactions navigate to outside the holder journey`(
        @TestParameter action: (
            ComposeContentTestRule,
            TestNavHostController,
            OnBackPressedDispatcherOwner?
        ) -> Unit = namedTestValues(
            "Tapping primary button" to { rule, _, _ ->
                rule.performButtonClick()
            },
            "Popping navigation backstack" to { _, controller, _ ->
                controller.popBackStack()
            },
            "Performing a back press via dispatcher" to { _, _, dispatcher ->
                dispatcher?.onBackPressedDispatcher?.onBackPressed()
            }
        )
    ) = runTest {
        composeTestRule.run {
            setContent {
                controller = prepareController(LocalContext.current)
                onBackPressedDispatcherOwner = LocalOnBackPressedDispatcherOwner.current
                Render()
            }

            repeat(2) {
                performButtonClick()
            }

            waitUntil {
                UnfulfillableErrorRoute == controller.currentBackStackEntry
                    ?.toRoute<UnfulfillableErrorRoute>()
            }

            action(composeTestRule, controller, onBackPressedDispatcherOwner)
            waitForIdle()

            waitUntil {
                "start" == controller.currentDestination?.route
            }
        }
    }

    @Composable
    private fun Render() {
        CompositionLocalProvider(
            LocalMetroViewModelFactory provides viewModelFactory
        ) {
            GdsTheme {
                NavHost(
                    navController = controller,
                    startDestination = "start"
                ) {
                    startRouteButton {
                        controller.navigate(HolderRoutes)
                    }

                    stubHolderRoute {
                        controller.navigateToUnfulfillableError()
                    }

                    configureUnfulfillableErrorRoute()
                }
            }
        }
    }
}

private fun ComposeContentTestRule.performButtonClick() {
    onNodeWithRole(Role.Button).performClick()
    waitForIdle()
}

private fun NavGraphBuilder.stubHolderRoute(onClick: () -> Unit) {
    composable<HolderRoutes> {
        Button(
            onClick = onClick
        ) {
            Text("Click")
        }
    }
}

private fun NavGraphBuilder.startRouteButton(onClick: () -> Unit) {
    composable(
        route = "start"
    ) {
        Button(
            onClick = onClick
        ) {
            Text("Click")
        }
    }
}
