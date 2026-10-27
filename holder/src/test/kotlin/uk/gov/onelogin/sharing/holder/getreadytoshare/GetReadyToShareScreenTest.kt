package uk.gov.onelogin.sharing.holder.getreadytoshare

import android.content.Context
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityOptionsCompat
import androidx.test.core.app.ApplicationProvider
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.namedTestValues
import com.google.testing.junit.testparameterinjector.TestParameter
import io.mockk.mockk
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.logging.analytics.FakeAnalyticsLogger
import uk.gov.logging.api.v3.MemorisedLogger
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.core.activity.registry.ActivityResultLauncherExt.ProvideActivityResultRegistry
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.prerequisites.usecases.ResolveHolderPrerequisiteAction
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.prerequisites.api.state.BluetoothState
import uk.gov.onelogin.sharing.prerequisites.api.usecases.RetryPrerequisitesNavigator
import uk.gov.onelogin.sharing.prerequisites.api.usecases.RetryPrerequisitesNavigator.NavigationEvent
import uk.gov.onelogin.sharing.prerequisites.impl.MissingPrerequisites
import uk.gov.onelogin.sharing.prerequisites.permissions.FakePermissionChecker
import uk.gov.onelogin.sharing.prerequisites.usecases.RetryPrerequisitesNavigatorExt.from

@RunWith(RobolectricTestParameterInjector::class)
class GetReadyToShareScreenTest {

    @get:Rule(order = 0)
    val dispatcherRule = MainDispatcherRule()

    @get:Rule(order = 1)
    val screenRule = GetReadyToShareScreenRule(
        resources = ApplicationProvider.getApplicationContext<Context>().resources
    )

    private var navigatorEvents = mutableListOf<NavigationEvent>()
    private var missingPrerequisites = mutableListOf(
        MissingPrerequisites.Bluetooth(state = BluetoothState.PermissionNotGranted)
    )

    private val logger = MemorisedLogger()

    private val orchestrator by lazy {
        FakeOrchestrator(
            initialHolderState = MutableStateFlow(initialHolderState)
        )
    }
    private val resolver by lazy {
        ResolveHolderPrerequisiteAction(
            logger = logger,
            orchestrator = orchestrator,
            permissionChecker = FakePermissionChecker()
        )
    }
    private val initialHolderState: HolderSessionState by lazy {
        HolderSessionState.Preflight(
            missingPrerequisites = missingPrerequisites
        ) { }
    }

    private val navigator by lazy {
        RetryPrerequisitesNavigator.from<HolderSessionState>(
            navigatorEvents.asFlow()
        )
    }

    private val analyticsLogger by lazy {
        FakeAnalyticsLogger()
    }

    private val viewModel by lazy {
        GetReadyToShareViewModel(
            analyticsLogger = analyticsLogger,
            navigator = navigator,
            orchestrator = orchestrator,
            resolver = resolver,
            dispatcher = dispatcherRule.testDispatcher,
            mainDispatcher = dispatcherRule.testDispatcher
        )
    }

    @Test
    fun `UI contains the correct elements`() = verifyUiState { Render() }

    @Test
    fun `Preview function contains the correct elements`() = verifyUiState {
        GetReadyToSharePreview()
    }

    private fun verifyUiState(content: @Composable () -> Unit) {
        runTest {
            screenRule.run {
                setContent { content() }
                assertValidHeader()
                assertValidBodyContents()
                assertValidAllowBluetoothButton()
                assertValidFindOutMoreButton()
            }
        }
    }

    @Test
    fun `Rendering the UI sends an analytics event`() = runTest {
        screenRule.run {
            setContent { Render() }
            waitUntil { analyticsLogger.size != 0 }
        }
    }

    @Test
    fun `Tapping resolve action defers to an activity result contract`() = runTest {
        screenRule.run {
            var hasCalledActivityResult = false
            val testRegistry = object : ActivityResultRegistry() {
                override fun <I, O> onLaunch(
                    requestCode: Int,
                    contract: ActivityResultContract<I, O>,
                    input: I,
                    options: ActivityOptionsCompat?
                ) {
                    hasCalledActivityResult = true
                    dispatchResult(requestCode, Unit)
                }
            }

            setContent {
                ProvideActivityResultRegistry(testRegistry) {
                    Render()
                }
            }

            performAllowActionClick()

            waitUntil("Hasn't called the activity result contract!") {
                hasCalledActivityResult
            }
        }
    }

    @Test
    fun `Allowing bluetooth sends an analytics event`() = runTest {
        screenRule.run {
            setContent {
                ProvideActivityResultRegistry(mockk(relaxed = true)) {
                    Render()
                }
            }
            waitUntil { analyticsLogger.size == 1 }

            performAllowActionClick()
            waitUntil { analyticsLogger.size == 2 }

            assertThat(
                analyticsLogger,
                contains(
                    GetReadyToShareTestData.expectedScreenEvent,
                    GetReadyToShareTestData.expectedAllowBluetoothButtonEvent
                )
            )
        }
    }

    @Test
    fun `Finding out more sends an analytics event`() = runTest {
        screenRule.run {
            setContent { Render() }
            waitUntil { analyticsLogger.size == 1 }

            performFindOutMoreClick()
            waitUntil { analyticsLogger.size == 2 }

            assertThat(
                analyticsLogger,
                contains(
                    GetReadyToShareTestData.expectedScreenEvent,
                    GetReadyToShareTestData.expectedFindOutMoreButtonEvent
                )
            )
        }
    }

    @Composable
    private fun Render() {
        GdsTheme {
            GetReadyToShareScreen(
                viewModel = viewModel
            )
        }
    }
}
