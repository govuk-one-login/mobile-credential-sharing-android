package uk.gov.onelogin.sharing.ui.impl

import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.annotation.UiThreadTest
import com.google.testing.junit.testparameterinjector.TestParameters
import dev.zacsweers.metro.createGraphFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.onelogin.sharing.holder.HolderStateToNavigationRoute
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.sdk.FakeCredentialPresenter
import uk.gov.onelogin.sharing.ui.impl.di.HolderUiGraph

@RunWith(RobolectricTestParameterInjector::class)
class ShareCredentialTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val appGraph by lazy {
        createTestAppGraph()
    }

    private val holderGraph by lazy {
        createTestHolderGraph(appGraph)
    }

    private lateinit var controller: TestNavHostController

    @Test
    @UiThreadTest
    fun `renders holder flow`() = runTest {
        val presenter = FakeCredentialPresenter(
            appGraph = appGraph,
            orchestrator = holderGraph.holderOrchestrator()
        )

        composeTestRule.setContent {
            ShareCredential(component = presenter)
        }

        composeTestRule.waitForIdle()
    }

    @TestParameters(valuesProvider = HolderStateToNavigationRoute::class)
    @Test
    @UiThreadTest
    fun `Session state maps to navigation routes`(
        state: HolderSessionState,
        assertion: TestNavHostController.() -> Boolean
    ) = runTest {
        val orchestrator = FakeOrchestrator(
            initialHolderState = MutableStateFlow(state)
        )
        val presenter = FakeCredentialPresenter(
            appGraph = appGraph,
            orchestrator = orchestrator
        )

        composeTestRule.setContent {
            val uiGraph = remember(presenter.appGraph, presenter.orchestrator) {
                createGraphFactory<HolderUiGraph.Factory>()
                    .create(presenter.appGraph, presenter.orchestrator)
            }
            controller = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }

            ShareCredential(
                holderSessionState = orchestrator.holderSessionState,
                viewModelFactory = uiGraph.metroViewModelFactory,
                navController = controller
            )
        }

        composeTestRule.waitForIdle()

        composeTestRule.waitUntil {
            assertion(controller)
        }
    }
}
