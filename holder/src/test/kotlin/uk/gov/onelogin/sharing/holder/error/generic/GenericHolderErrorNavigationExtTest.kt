package uk.gov.onelogin.sharing.holder.error.generic

import android.content.Context
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.performClick
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.DialogNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import androidx.navigation.toRoute
import androidx.test.annotation.UiThreadTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.fail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.runner.RunWith
import uk.gov.android.ui.componentsv2.rules.ComposeContentTestRuleExtensions.onNodeWithRole
import uk.gov.logging.analytics.FakeAnalyticsLogger
import uk.gov.onelogin.sharing.holder.TestViewModelFactory
import uk.gov.onelogin.sharing.holder.error.generic.GenericHolderErrorNavigationExt.configureGenericHolderError
import uk.gov.onelogin.sharing.holder.error.generic.GenericHolderErrorNavigationExt.navigateToGenericHolderError
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.session.SessionError
import uk.gov.onelogin.sharing.orchestration.session.SessionErrorReason

@RunWith(AndroidJUnit4::class)
class GenericHolderErrorNavigationExtTest {

    @get:Rule
    val composeTestRule = GenericHolderErrorScreenRule()

    private lateinit var controller: TestNavHostController

    private val analyticsLogger = FakeAnalyticsLogger()
    private var initialHolderState: HolderSessionState = HolderSessionState.Complete.Failed(
        SessionError(
            "This is a navigation test",
            SessionErrorReason.InvalidSessionDataPayload
        )
    )

    private val orchestrator by lazy {
        FakeOrchestrator(
            initialHolderState = MutableStateFlow(initialHolderState)
        )
    }

    private val viewModel by lazy {
        GenericHolderErrorViewModel(
            analyticsLogger = analyticsLogger,
            orchestrator = orchestrator
        )
    }

    private val viewModelFactory: MetroViewModelFactory = TestViewModelFactory(
        viewModelProviders = mapOf(
            GenericHolderErrorViewModel::class to { viewModel }
        )
    )

    @Test
    @UiThreadTest
    fun `Can navigate to generic holder error screen`() = runTest {
        composeTestRule.run {
            setContent {
                controller = prepareController(LocalContext.current)

                Render()
            }

            onNodeWithRole(Role.Button)
                .performClick()

            waitUntil {
                controller.currentBackStackEntry?.destination
                    ?.hasRoute<GenericHolderErrorRoute>() ?: false
            }
        }
    }

    @Test
    @UiThreadTest
    fun `Back press navigates to the previous screen`() = runTest {
        composeTestRule.run {
            setContent {
                controller = prepareController(LocalContext.current)
                Render()
            }

            onNodeWithRole(Role.Button)
                .performClick()

            assertValidUi()

            runOnUiThread {
                controller.popBackStack()
            }
            waitForIdle()

            waitUntil(
                "Didn't navigate back! Current destination: " +
                    controller.currentBackStackEntry?.destination?.route
            ) {
                controller.currentBackStackEntry?.destination
                    ?.route?.let { it == "entry" } ?: false
            }
        }
    }

    private fun prepareController(context: Context): TestNavHostController =
        TestNavHostController(context).apply {
            navigatorProvider.addNavigator(ComposeNavigator())
            navigatorProvider.addNavigator(DialogNavigator())
        }

    @Composable
    private fun Render() {
        CompositionLocalProvider(
            LocalMetroViewModelFactory provides viewModelFactory
        ) {
            NavHost(
                navController = controller,
                startDestination = "entry"
            ) {
                composable("entry") {
                    Button(
                        onClick = {
                            controller.navigateToGenericHolderError {
                                popUpTo("entry")
                            }
                        }
                    ) {
                        Text("This is a unit test")
                    }
                }
                configureGenericHolderError()
            }
        }
    }
}
