package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth.denied

import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.Composable
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
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.fail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import uk.gov.logging.analytics.FakeAnalyticsLogger
import uk.gov.logging.api.v3.MemorisedLogger
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
class DeviceBleDeniedScreenTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = DeviceBleDeniedScreenRule(createComposeRule())

    private var recheckCount = 0

    private val holderState by lazy {
        MutableStateFlow<HolderSessionState>(
            HolderSessionState.Preflight(
                missingPrerequisites = listOf(
                    MissingPrerequisites.Bluetooth(BluetoothState.PoweredOff)
                ),
                enableBluetoothDenied = true
            ) { recheckCount++ }
        )
    }
    private val orchestrator by lazy { FakeOrchestrator(initialHolderState = holderState) }
    private val analyticsLogger by lazy { FakeAnalyticsLogger() }

    private val viewModel by lazy {
        DeviceBleDeniedViewModel(
            analyticsLogger = analyticsLogger,
            orchestrator = orchestrator,
            resolver = ResolveHolderPrerequisiteAction(
                logger = MemorisedLogger(),
                orchestrator = orchestrator,
                permissionChecker = FakePermissionChecker()
            ),
            dispatcher = dispatcherRule.testDispatcher
        )
    }

    private val viewModelFactory = object : MetroViewModelFactory() {
        override val viewModelProviders: Map<KClass<out ViewModel>, () -> ViewModel>
            get() = mapOf(DeviceBleDeniedViewModel::class to { viewModel })

        override val assistedFactoryProviders:
            Map<KClass<out ViewModel>, () -> ViewModelAssistedFactory>
            get() = emptyMap()

        override val manualAssistedFactoryProviders:
            Map<KClass<out ManualViewModelAssistedFactory>, () -> ManualViewModelAssistedFactory>
            get() = emptyMap()
    }

    private var launchedAction: Any? = null
    private val testRegistry = object : ActivityResultRegistry() {
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
    fun `Displays the screen content`() = runTest {
        composeTestRule.run {
            setContent { DeviceBleDeniedContent(onTurnOnBluetooth = {}, onCancel = {}) }
            assertValidUi()
        }
    }

    @Test
    fun `Displaying the screen sends a screen view event`() = runTest {
        composeTestRule.run {
            setContent { Render() }
            waitUntil { analyticsLogger.size != 0 }
        }
        assertThat(analyticsLogger, contains(DeviceBleDeniedTestData.expectedScreenEvent))
    }

    @Test
    fun `Tapping Turn on Bluetooth launches the enable Bluetooth dialogue`() = runTest {
        composeTestRule.run {
            setContent { Render() }

            performTurnOnBluetoothClick()

            waitUntil { launchedAction != null }
            assertEquals(PrerequisiteAction.EnableBluetooth, launchedAction)
            waitUntil { analyticsLogger.size >= 2 }
        }
        assertThat(
            analyticsLogger,
            contains(
                DeviceBleDeniedTestData.expectedScreenEvent,
                DeviceBleDeniedTestData.expectedTurnOnBluetoothEvent
            )
        )
    }

    @Test
    fun `Returning from the enable Bluetooth dialogue rechecks prerequisites once`() = runTest {
        val lifecycleOwner = TestLifecycleOwner(
            initialState = Lifecycle.State.RESUMED,
            coroutineDispatcher = Dispatchers.Unconfined
        )

        composeTestRule.run {
            setContent {
                CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                    Render()
                }
            }
            waitForIdle()

            // The test registry returns the dialogue's result straight away.
            performTurnOnBluetoothClick()
            waitUntil { launchedAction != null }
            runOnIdle {
                lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
                lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            }

            waitUntil("Prerequisites were not rechecked") { recheckCount == 1 }
            waitForIdle()
            assertEquals(1, recheckCount, "Prerequisites should only be rechecked once")
        }
    }

    @Test
    fun `Prerequisites are only rechecked when resuming after a pause`() = runTest {
        val lifecycleOwner = TestLifecycleOwner(
            initialState = Lifecycle.State.STARTED,
            coroutineDispatcher = Dispatchers.Unconfined
        )

        composeTestRule.run {
            setContent {
                CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                    Render()
                }
            }
            waitForIdle()

            runOnIdle { lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME) }
            waitForIdle()
            assertEquals(0, recheckCount, "The initial resume shouldn't recheck")

            runOnIdle {
                lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
                lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            }

            waitUntil { recheckCount == 1 }
        }
    }

    @Test
    fun `Tapping cancel cancels the journey without asking for confirmation`() = runTest {
        composeTestRule.run {
            setContent { Render() }

            performCancelClick()

            waitUntil("Journey should have been cancelled!") { orchestrator.cancelCount == 1 }
        }
        assertEquals(0, orchestrator.resetCount)
    }

    @Test
    fun `Tapping cancel sends the cancel button event`() = runTest {
        composeTestRule.run {
            setContent { Render() }

            performCancelClick()

            waitUntil("Cannot find expected event!: $analyticsLogger") {
                analyticsLogger.contains(DeviceBleDeniedTestData.expectedCancelEvent)
            }
        }
    }

    @Test
    fun `Pressing back behaves the same as tapping cancel`() = runTest {
        var onBackPressedDispatcher: OnBackPressedDispatcher? = null
        composeTestRule.run {
            setContent {
                onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current
                    ?.onBackPressedDispatcher
                Render()
            }
            waitForIdle()

            runOnUiThread {
                onBackPressedDispatcher?.onBackPressed()
                    ?: fail("Cannot find back press dispatcher!")
            }

            waitUntil("Journey should have been cancelled!") { orchestrator.cancelCount == 1 }
            waitUntil("Cannot find expected event!: $analyticsLogger") {
                analyticsLogger.contains(DeviceBleDeniedTestData.expectedCancelEvent)
            }
        }
        assertEquals(0, orchestrator.resetCount)
    }

    @Test
    @Config(qualifiers = "cy")
    fun `Displaying the screen in Welsh sends analytics events in English`() = runTest {
        composeTestRule.run {
            setContent { Render() }

            performTurnOnBluetoothClick()
            waitUntil { analyticsLogger.size >= 2 }
            performCancelClick()
            waitUntil { analyticsLogger.size >= 3 }
        }
        assertThat(
            analyticsLogger,
            contains(
                DeviceBleDeniedTestData.expectedScreenEvent,
                DeviceBleDeniedTestData.expectedTurnOnBluetoothEvent,
                DeviceBleDeniedTestData.expectedCancelEvent
            )
        )
    }

    @Test
    fun `Leaving Preflight replaces the screen content with a loading spinner`() = runTest {
        composeTestRule.run {
            setContent { Render() }
            assertTitleIsDisplayed()

            holderState.update { HolderSessionState.ReadyToPresent }

            waitUntilLoadingSpinnerExists()
            assertLoadingSpinnerIsDisplayed()
            assertTitleDoesNotExist()
        }
    }

    @Test
    fun `Injected viewmodel is used by default`() = runTest {
        composeTestRule.run {
            setContent {
                CompositionLocalProvider(LocalMetroViewModelFactory provides viewModelFactory) {
                    DeviceBleDeniedScreen()
                }
            }

            assertTitleIsDisplayed()
            waitUntil { analyticsLogger.size != 0 }
        }
        assertThat(analyticsLogger, contains(DeviceBleDeniedTestData.expectedScreenEvent))
    }

    @Test
    fun `Content preview renders the screen content`() = runTest {
        composeTestRule.run {
            setContent { DeviceBleDeniedContentPreview() }
            assertValidUi()
        }
    }

    @Test
    fun `Loading preview renders the loading content`() = runTest {
        composeTestRule.run {
            setContent { DeviceBleDeniedLoadingContentPreview() }
            assertLoadingSpinnerIsDisplayed()
        }
    }

    @Composable
    private fun Render() {
        ProvideActivityResultRegistry(testRegistry) {
            DeviceBleDeniedScreen(viewModel = viewModel)
        }
    }
}
