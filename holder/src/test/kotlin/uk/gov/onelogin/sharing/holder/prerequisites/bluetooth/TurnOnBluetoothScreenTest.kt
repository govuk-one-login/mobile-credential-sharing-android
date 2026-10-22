package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth

import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import kotlin.getValue
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasItem
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.logging.api.v3.MemorisedLogger
import uk.gov.onelogin.sharing.analytics.StoredAnalyticsLogger
import uk.gov.onelogin.sharing.analytics.StoredAnalyticsLoggerMatchers.hasSentEvents
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.core.activity.registry.ActivityResultLauncherExt.ProvideActivityResultRegistry
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.prerequisites.usecases.ResolveHolderPrerequisiteAction
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.prerequisites.api.PrerequisiteAction
import uk.gov.onelogin.sharing.prerequisites.api.state.BluetoothState
import uk.gov.onelogin.sharing.prerequisites.impl.MissingPrerequisites
import uk.gov.onelogin.sharing.prerequisites.permissions.FakePermissionChecker

@RunWith(AndroidJUnit4::class)
class TurnOnBluetoothScreenTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = TurnOnBluetoothScreenRule(createComposeRule())

    private var hasRecheckedPrerequisites = false
    private var recheckCount = 0

    private val initialHolderState: HolderSessionState by lazy {
        HolderSessionState.Preflight(
            missingPrerequisites = listOf(
                MissingPrerequisites.Bluetooth(BluetoothState.PoweredOff)
            )
        ) {
            hasRecheckedPrerequisites = true
            recheckCount++
        }
    }
    private val holderState by lazy {
        MutableStateFlow(initialHolderState)
    }
    private val orchestrator by lazy {
        FakeOrchestrator(initialHolderState = holderState)
    }

    private val logger = MemorisedLogger()

    private val analyticsLogger by lazy {
        StoredAnalyticsLogger()
    }

    private val resolver by lazy {
        ResolveHolderPrerequisiteAction(
            logger = logger,
            orchestrator = orchestrator,
            permissionChecker = FakePermissionChecker()
        )
    }

    private val viewModel by lazy {
        TurnOnBluetoothViewModel(
            analyticsLogger = analyticsLogger,
            orchestrator = orchestrator,
            resolver = resolver,
            dispatcher = dispatcherRule.testDispatcher
        )
    }

    private val viewModelFactory = object : MetroViewModelFactory() {
        override val viewModelProviders: Map<KClass<out ViewModel>, () -> ViewModel>
            get() = mapOf(TurnOnBluetoothViewModel::class to { viewModel })

        override val assistedFactoryProviders:
            Map<KClass<out ViewModel>, () -> ViewModelAssistedFactory>
            get() = emptyMap()

        override val manualAssistedFactoryProviders:
            Map<KClass<out ManualViewModelAssistedFactory>, () -> ManualViewModelAssistedFactory>
            get() = emptyMap()
    }
    var launchedAction: Any? = null
    val testRegistry = object : ActivityResultRegistry() {
        override fun <I, O> onLaunch(
            requestCode: Int,
            contract: ActivityResultContract<I, O>,
            input: I,
            options: ActivityOptionsCompat?
        ) {
            launchedAction = input
            dispatchResult(requestCode, Unit)
        }
    }

    @Test
    fun `Displays the Turn on Bluetooth screen content`() = runTest {
        composeTestRule.run {
            setContent { TurnOnBluetoothContent(onTurnOnBluetooth = {}) }
            assertTitleIsDisplayed()
            assertBodyIsDisplayed()
            assertTurnOnBluetoothButtonIsDisplayed()
        }
    }

    @Test
    fun `Tapping Turn on Bluetooth launches the enable Bluetooth dialogue`() = runTest {
        composeTestRule.run {
            setContent {
                ProvideActivityResultRegistry(testRegistry) {
                    TurnOnBluetoothScreen(viewModel = viewModel)
                }
            }

            performTurnOnBluetoothClick()

            waitUntil { launchedAction != null }
            assertEquals(PrerequisiteAction.EnableBluetooth, launchedAction)
        }
        assertThat(
            analyticsLogger,
            hasSentEvents(hasItem(TurnOnBluetoothViewModelTestData.expectedTurnOnBluetoothEvent))
        )
    }

    @Test
    fun `Prerequisites are rechecked after enable Bluetooth dialogue returns`() = runTest {
        composeTestRule.run {
            setContent {
                ProvideActivityResultRegistry(testRegistry) {
                    TurnOnBluetoothScreen(viewModel = viewModel)
                }
            }

            performTurnOnBluetoothClick()

            waitUntil("Prerequisites were not rechecked") {
                hasRecheckedPrerequisites
            }
        }
    }

    @Test
    fun `Displaying the screen sends a screen view event`() = runTest {
        composeTestRule.run {
            setContent { TurnOnBluetoothScreen(viewModel = viewModel) }
            waitUntil { analyticsLogger.sentAnalyticsEvents.isNotEmpty() }
        }
        assertThat(
            analyticsLogger,
            hasSentEvents(hasItem(TurnOnBluetoothViewModelTestData.expectedScreenEvent))
        )
    }

    @Test
    fun `Closing the journey shows a loading spinner`() = runTest {
        composeTestRule.run {
            setContent { TurnOnBluetoothScreen(viewModel = viewModel) }
            assertTitleIsDisplayed()
            holderState.update { HolderSessionState.SendingTermination }
            waitUntilLoadingSpinnerExists()
            assertLoadingSpinnerIsDisplayed()
            assertTitleDoesNotExist()
        }
    }

    @Test
    fun `Prerequisites are rechecked on resume after tapping Turn on Bluetooth`() = runTest {
        val lifecycleOwner = TestLifecycleOwner(
            initialState = Lifecycle.State.RESUMED,
            coroutineDispatcher = Dispatchers.Unconfined
        )

        composeTestRule.run {
            setContent {
                CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                    ProvideActivityResultRegistry(testRegistry) {
                        TurnOnBluetoothScreen(viewModel = viewModel)
                    }
                }
            }

            performTurnOnBluetoothClick()
            waitUntil { recheckCount == 1 }

            runOnIdle {
                lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
                lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            }

            waitUntil { recheckCount == 2 }
        }
    }

    @Test
    fun `Prerequisites are not rechecked on resume before tapping Turn on Bluetooth`() = runTest {
        val lifecycleOwner = TestLifecycleOwner(
            initialState = Lifecycle.State.RESUMED,
            coroutineDispatcher = Dispatchers.Unconfined
        )

        composeTestRule.run {
            setContent {
                CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                    ProvideActivityResultRegistry(testRegistry) {
                        TurnOnBluetoothScreen(viewModel = viewModel)
                    }
                }
            }

            runOnIdle {
                lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
                lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            }
            waitForIdle()
        }

        assertEquals(0, recheckCount)
    }

    @Test
    fun `Injected viewmodel is used by default`() = runTest {
        composeTestRule.run {
            setContent {
                CompositionLocalProvider(LocalMetroViewModelFactory provides viewModelFactory) {
                    TurnOnBluetoothScreen()
                }
            }

            assertTitleIsDisplayed()
            waitUntil { analyticsLogger.sentAnalyticsEvents.isNotEmpty() }
        }

        assertThat(
            analyticsLogger,
            hasSentEvents(hasItem(TurnOnBluetoothViewModelTestData.expectedScreenEvent))
        )
    }
}
