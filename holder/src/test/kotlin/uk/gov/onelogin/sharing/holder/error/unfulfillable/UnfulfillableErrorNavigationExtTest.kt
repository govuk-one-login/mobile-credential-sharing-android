package uk.gov.onelogin.sharing.holder.error.unfulfillable

import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import androidx.navigation.toRoute
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.namedTestValues
import com.google.testing.junit.testparameterinjector.TestParameter
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.android.ui.componentsv2.rules.ComposeContentTestRuleExtensions.onNodeWithRole
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.onelogin.sharing.holder.HolderRoutes
import uk.gov.onelogin.sharing.holder.error.generic.GenericHolderErrorScreenRule
import uk.gov.onelogin.sharing.holder.error.unfulfillable.UnfulfillableErrorNavigationExt.configureUnfulfillableErrorRoute
import uk.gov.onelogin.sharing.holder.error.unfulfillable.UnfulfillableErrorNavigationExt.navigateToUnfulfillableError
import uk.gov.onelogin.sharing.holder.error.unfulfillable.UnfulfillableErrorRoute
import uk.gov.onelogin.sharing.holder.navigation.TestNavHostControllerHelper.prepareController
import kotlin.test.Test

@RunWith(RobolectricTestParameterInjector::class)
class UnfulfillableErrorNavigationExtTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var controller: TestNavHostController
    private var onBackPressedDispatcherOwner: OnBackPressedDispatcherOwner? = null

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
//            "Tapping primary button" to { rule, _, _ ->
//                rule.performGoBackToDocumentsClick()
//            },
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
