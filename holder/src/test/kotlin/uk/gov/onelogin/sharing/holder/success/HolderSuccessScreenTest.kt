package uk.gov.onelogin.sharing.holder.success

import android.content.res.Resources
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.DialogNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import androidx.navigation.toRoute
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.namedTestValues
import com.google.testing.junit.testparameterinjector.TestParameter
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.holder.R
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator

@RunWith(RobolectricTestParameterInjector::class)
class HolderSuccessScreenTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var controller: TestNavHostController

    private var hasExitedJourney = false
    private var hasExitedJourneyCount = 0

    private val orchestrator by lazy {
        FakeOrchestrator()
    }

    private val viewModelFactory: MetroViewModelFactory = mockk(relaxed = true)

    private val viewModel by lazy {
        HolderSuccessViewModel(
            orchestrator = orchestrator,
            dispatcher = dispatcherRule.testDispatcher
        )
    }

    @Test
    fun `Displays unfulfillable request title`(
        @TestParameter content: @Composable () -> Unit = namedTestValues(
            "Screen" to {
                CompositionLocalProvider(
                    LocalMetroViewModelFactory provides viewModelFactory
                ) {
                    HolderSuccessScreen(
                        viewModel = viewModel
                    )
                }
            },
            "Preview" to { HolderSuccessScreenPreview() }
        )
    ) = runTest(dispatcherRule.testDispatcher) {
        lateinit var resources: Resources
        composeTestRule.setContent {
            resources = LocalResources.current
            content()
        }

        composeTestRule.onNodeWithText(
            resources.getString(R.string.holder_success_unfulfillable_request_title)
        ).assertIsDisplayed()
    }

    @Test
    fun `Back button resets the journey`() = runTest(dispatcherRule.testDispatcher) {
        composeTestRule.run {
            var backPressedDispatcher: OnBackPressedDispatcherOwner? = null
            setContent {
                backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current
                RenderNavHost()
            }

            runOnUiThread {
                controller.navigate(HolderSuccessRoute())
            }

            waitUntil {
                controller.currentBackStackEntry?.toRoute<HolderSuccessRoute>() != null
            }

            waitForIdle()

            runOnUiThread {
                backPressedDispatcher?.onBackPressedDispatcher?.onBackPressed()
            }

            waitUntil(
                "Didn't call `orchestrator.reset()` via the view model!"
            ) { orchestrator.resetCount == 1 }
            waitUntil(
                "Didn't call the 'onExitJourney' lambda!"
            ) { hasExitedJourney }
            waitUntil(
                "'onExitJourney' has an unexpected call count!: $hasExitedJourneyCount"
            ) { hasExitedJourneyCount == 1 }
        }
    }

    @Test
    fun `Immediately resets and exits without requiring user action`() = runTest(
        dispatcherRule.testDispatcher
    ) {
        composeTestRule.run {
            setContent {
                CompositionLocalProvider(
                    LocalMetroViewModelFactory provides viewModelFactory
                ) {
                    HolderSuccessScreen(
                        viewModel = viewModel,
                        immediatelyReset = true,
                        onExitJourney = {
                            hasExitedJourney = true
                            hasExitedJourneyCount++
                        }
                    )
                }
            }

            waitUntil("Didn't call orchestrator.reset() via viewmodel") {
                orchestrator.resetCount == 1
            }

            waitUntil("Didn't call the onExitJourney") {
                hasExitedJourney
            }

            waitUntil("Unexpected hasExitedJourneyCount call count: $hasExitedJourneyCount") {
                hasExitedJourneyCount == 1
            }
        }
    }

    @Composable
    private fun RenderNavHost() {
        val context = LocalContext.current
        controller = TestNavHostController(context).apply {
            navigatorProvider.addNavigator(ComposeNavigator())
            navigatorProvider.addNavigator(DialogNavigator())
        }

        CompositionLocalProvider(
            LocalMetroViewModelFactory provides viewModelFactory
        ) {
            NavHost(
                navController = controller,
                startDestination = "previous"
            ) {
                composable("previous") {}
                composable<HolderSuccessRoute> {
                    HolderSuccessScreen(
                        viewModel = viewModel,
                        onExitJourney = {
                            hasExitedJourney = true
                            hasExitedJourneyCount++
                        }
                    )
                }
            }
        }
    }
}
