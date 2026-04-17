package uk.gov.onelogin.sharing.holder.prerequisites.retry

import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.core.app.ActivityOptionsCompat
import com.google.testing.junit.testparameterinjector.TestParameters
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.core.activity.registry.ActivityResultLauncherExt.ProvideActivityResultRegistry
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.prerequisites.usecases.ResolveHolderPrerequisiteAction
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.prerequisites.MissingPrerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.state.BluetoothState
import uk.gov.onelogin.sharing.orchestration.prerequisites.ui.RetryPrerequisitesContentRule
import uk.gov.onelogin.sharing.orchestration.prerequisites.ui.RetryPrerequisitesNavigatorAssertions
import uk.gov.onelogin.sharing.orchestration.prerequisites.usecases.RetryPrerequisitesNavigator
import uk.gov.onelogin.sharing.orchestration.prerequisites.usecases.RetryPrerequisitesNavigator.NavigationEvent
import uk.gov.onelogin.sharing.orchestration.prerequisites.usecases.RetryPrerequisitesNavigatorExt.from

@RunWith(RobolectricTestParameterInjector::class)
class RetryHolderPrerequisitesScreenTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = RetryPrerequisitesContentRule(createComposeRule())

    private var navigatorEvents = mutableListOf<NavigationEvent?>()
    private var missingPrerequisites = mutableListOf(
        MissingPrerequisite.Bluetooth(state = BluetoothState.PermissionNotGranted)
    )

    private val logger = SystemLogger()

    private val orchestrator by lazy {
        FakeOrchestrator(
            initialHolderState = MutableStateFlow(initialHolderState)
        )
    }
    private val resolver by lazy {
        ResolveHolderPrerequisiteAction(
            logger = logger,
            orchestrator = orchestrator
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

    private val viewModel by lazy {
        RetryHolderPrerequisitesViewModel(
            navigator = navigator,
            orchestrator = orchestrator,
            resolver = resolver,
            dispatcher = dispatcherRule.testDispatcher
        )
    }

    @Test
    @TestParameters(valuesProvider = RetryPrerequisitesNavigatorAssertions::class)
    fun `Calls lambda based on navigation event`(
        event: NavigationEvent,
        assertion: RetryPrerequisitesContentRule.() -> Unit
    ) = runTest(dispatcherRule.testDispatcher) {
        navigatorEvents.add(event)
        composeTestRule.run {
            setContent {
                RetryHolderPrerequisitesScreen(
                    viewModel = viewModel,
                    onPassPrerequisites = { composeTestRule.updateHasPassedPrerequisites() },
                    onUnrecoverableError = { composeTestRule.updateHasUnrecoverableError() }
                )
            }

            assertion(composeTestRule)
        }
    }

    @Test
    fun `Tapping resolve action defers to an activity result contract`() = runTest {
        composeTestRule.run {
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
                    RetryHolderPrerequisitesScreen(
                        viewModel = viewModel,
                        onPassPrerequisites = { composeTestRule.updateHasPassedPrerequisites() },
                        onUnrecoverableError = { composeTestRule.updateHasUnrecoverableError() }
                    )
                }
            }

            performResolveActionClick()

            waitUntil("Hasn't called the activity result contract!") {
                hasCalledActivityResult
            }
        }
    }
}
