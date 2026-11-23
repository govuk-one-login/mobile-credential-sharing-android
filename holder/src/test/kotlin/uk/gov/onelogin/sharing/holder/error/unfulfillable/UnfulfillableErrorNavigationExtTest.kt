package uk.gov.onelogin.sharing.holder.error.unfulfillable

import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import androidx.navigation.toRoute
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.runner.RunWith
import uk.gov.android.ui.componentsv2.rules.ComposeContentTestRuleExtensions.onNodeWithRole
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.onelogin.sharing.holder.HolderRoutes
import uk.gov.onelogin.sharing.holder.error.unfulfillable.UnfulfillableErrorNavigationExt.configureUnfulfillableErrorRoute
import uk.gov.onelogin.sharing.holder.error.unfulfillable.UnfulfillableErrorNavigationExt.navigateToUnfulfillableError
import uk.gov.onelogin.sharing.holder.navigation.TestNavHostControllerHelper.prepareController
import kotlin.test.Test

@RunWith(AndroidJUnit4::class)
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

            onNodeWithRole(Role.Button).performClick()
            onNodeWithRole(Role.Button).performClick()

            waitForIdle()

            assertThat(
                controller.currentBackStackEntry?.toRoute(),
                equalTo(UnfulfillableErrorRoute)
            )
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
                    controller.navigateToUnfulfillableError {
                        popUpTo<HolderRoutes> {
                            inclusive = true
                        }
                    }
                }

                configureUnfulfillableErrorRoute()
            }
        }
    }
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