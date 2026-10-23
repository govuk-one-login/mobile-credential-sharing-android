package uk.gov.onelogin.sharing.holder.getreadytoshare

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.core.app.ActivityOptionsCompat
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.logging.analytics.FakeAnalyticsLogger
import uk.gov.logging.api.analytics.logging.MemorisedAnalyticsLogger
import uk.gov.logging.api.v3.MemorisedLogger
import uk.gov.onelogin.sharing.analytics.StoredAnalyticsLogger
import uk.gov.onelogin.sharing.analytics.SystemAnalyticsLogger
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

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val accessibilityRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val screenRule = GetReadyToShareScreenRule(accessibilityRule)

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
            dispatcher = dispatcherRule.testDispatcher
        )
    }

    @Test
    fun `UI contains the correct elements`() = runTest {
        screenRule.run {
            setContent { Render() }
            assertValidHeader()
            assertValidBodyContents()
            assertValidAllowBluetoothButton()
            assertValidFindOutMoreButton()
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

    @Composable
    private fun Render() {
        GdsTheme {
            GetReadyToShareScreen(
                viewModel = viewModel
            )
        }
    }
}
