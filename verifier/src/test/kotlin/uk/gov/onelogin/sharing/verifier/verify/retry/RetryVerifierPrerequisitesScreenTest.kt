package uk.gov.onelogin.sharing.verifier.verify.retry

import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.core.app.ActivityOptionsCompat
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
import uk.gov.onelogin.sharing.orchestration.verifier.prerequisites.usecases.ResolveVerifierPrerequisiteAction
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState
import uk.gov.onelogin.sharing.prerequisites.MissingPrerequisites
import uk.gov.onelogin.sharing.prerequisites.state.BluetoothState
import uk.gov.onelogin.sharing.prerequisites.ui.RetryPrerequisitesContentRule
import uk.gov.onelogin.sharing.prerequisites.usecases.RetryPrerequisitesNavigator
import uk.gov.onelogin.sharing.prerequisites.usecases.RetryPrerequisitesNavigator.NavigationEvent
import uk.gov.onelogin.sharing.prerequisites.usecases.RetryPrerequisitesNavigatorExt.from

@RunWith(RobolectricTestParameterInjector::class)
class RetryVerifierPrerequisitesScreenTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = RetryPrerequisitesContentRule(createComposeRule())

    private var navigatorEvents = mutableListOf<NavigationEvent>()
    private var missingPrerequisites = mutableListOf(
        MissingPrerequisites.Bluetooth(state = BluetoothState.PermissionNotGranted)
    )

    private val logger = SystemLogger()

    private val orchestrator by lazy {
        FakeOrchestrator(
            initialVerifierState = MutableStateFlow(initialState)
        )
    }
    private val resolver by lazy {
        ResolveVerifierPrerequisiteAction(
            logger = logger,
            orchestrator = orchestrator
        )
    }
    private val initialState: VerifierSessionState by lazy {
        VerifierSessionState.Preflight(
            missingPrerequisites = missingPrerequisites
        ) { }
    }

    private val navigator by lazy {
        RetryPrerequisitesNavigator.from<VerifierSessionState>(
            navigatorEvents.asFlow()
        )
    }

    private val viewModel by lazy {
        RetryVerifierPrerequisitesViewModel(
            navigator = navigator,
            orchestrator = orchestrator,
            resolver = resolver,
            dispatcher = dispatcherRule.testDispatcher
        )
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
                    RetryVerifierPrerequisitesScreen(
                        viewModel = viewModel
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
