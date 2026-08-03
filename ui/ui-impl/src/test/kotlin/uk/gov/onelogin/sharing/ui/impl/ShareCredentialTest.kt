package uk.gov.onelogin.sharing.ui.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.DialogNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.annotation.UiThreadTest
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.namedTestValues
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameters
import dev.zacsweers.metro.createGraphFactory
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.onelogin.sharing.holder.HolderStateToNavigationRoute
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.session.SessionError
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

    @Test
    @UiThreadTest
    fun `Close button exists for incomplete journeys`(
        @TestParameter state: HolderSessionState = namedTestValues(
            "Presenting QR code" to HolderSessionState.PresentingEngagement(""),
            "Awaiting consent" to HolderSessionState.AwaitingUserConsent(mockk(relaxed = true)),
            "Awaiting verifier resolution" to HolderSessionState.AwaitingVerifierResolution
        )
    ) = runTest {
        val presenter = FakeCredentialPresenter(
            appGraph = appGraph,
            orchestrator = FakeOrchestrator(
                initialHolderState = MutableStateFlow(state)
            )
        )

        composeTestRule.run {
            setContent { ShareCredential(component = presenter) }
            waitForIdle()
            onNodeWithContentDescription("Close").assertExists()
        }
    }

    @Test
    @UiThreadTest
    fun `Close button navigates to confirmation dialog for certain states`(
        @TestParameter state: HolderSessionState = namedTestValues(
            "Awaiting consent" to HolderSessionState.AwaitingUserConsent(mockk(relaxed = true)),
            "Awaiting verifier resolution" to HolderSessionState.AwaitingVerifierResolution
        )
    ) = runTest {
        val presenter = FakeCredentialPresenter(
            appGraph = appGraph,
            orchestrator = FakeOrchestrator(
                initialHolderState = MutableStateFlow(state)
            )
        )

        composeTestRule.run {
            setContent { SetupExtendedShareCredential(presenter) }
            waitForIdle()
            onNodeWithContentDescription("Close").performClick()

            waitUntil(
                "Unexpected route found!: ${controller.currentDestination?.route}"
            ) {
                controller.currentDestination?.route
                    ?.contains("HolderCancellationDialogRoute")
                    ?: false
            }
        }
    }

    @Test
    @UiThreadTest
    fun `Close button cancels the journey for certain states`(
        @TestParameter state: HolderSessionState = namedTestValues(
            "Presenting QR code" to HolderSessionState.PresentingEngagement(""),
        )
    ) = runTest {
        val orchestrator = FakeOrchestrator(
            initialHolderState = MutableStateFlow(state)
        )
        val presenter = FakeCredentialPresenter(
            appGraph = appGraph,
            orchestrator = orchestrator
        )

        composeTestRule.run {
            setContent { SetupExtendedShareCredential(presenter) }
            waitForIdle()
            onNodeWithContentDescription("Close").performClick()

            waitUntil(
                "Tapping close should have cancelled the journey!"
            ) {
                orchestrator.cancelCount == 1
            }
        }
    }

    @Composable
    private fun SetupExtendedShareCredential(presenter: FakeCredentialPresenter) {
        val uiGraph = remember(presenter.appGraph, presenter.orchestrator) {
            createGraphFactory<HolderUiGraph.Factory>()
                .create(presenter.appGraph, presenter.orchestrator)
        }
        val context = LocalContext.current
        controller = TestNavHostController(context).apply {
            navigatorProvider.addNavigator(ComposeNavigator())
            navigatorProvider.addNavigator(DialogNavigator())
        }
        val orchestrator = uiGraph.holderOrchestrator()

        ShareCredential(
            orchestrator = orchestrator,
            holderSessionState = presenter.orchestrator.holderSessionState,
            navController = controller,
            viewModelFactory = uiGraph.metroViewModelFactory
        )
    }

    @Test
    @UiThreadTest
    fun `Close button doesn't exist for complete journeys`(
        @TestParameter state: HolderSessionState = namedTestValues(
            "Cancelled" to HolderSessionState.Complete.Cancelled,
            "Failed" to HolderSessionState.Complete.Failed(
                SessionError(
                    "This is a UI test",
                    Exception()
                )
            ),
            "Success" to HolderSessionState.Complete.Success(
                HolderSessionState.Complete.SuccessReason.Approved
            )
        )
    ) = runTest {
        val presenter = FakeCredentialPresenter(
            appGraph = appGraph,
            orchestrator = FakeOrchestrator(
                initialHolderState = MutableStateFlow(state)
            )
        )

        composeTestRule.run {
            setContent { ShareCredential(component = presenter) }
            waitForIdle()
            onNodeWithContentDescription("Close").assertDoesNotExist()
        }
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
                navigatorProvider.addNavigator(DialogNavigator())
            }

            ShareCredential(
                orchestrator = orchestrator,
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
