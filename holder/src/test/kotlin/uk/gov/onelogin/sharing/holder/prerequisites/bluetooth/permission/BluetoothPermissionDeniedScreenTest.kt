package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth.permission

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
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.junit.Rule
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
class BluetoothPermissionDeniedScreenTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = BluetoothPermissionDeniedScreenRule(createComposeRule())

    private var hasRecheckedPrerequisites = false
    private var recheckCount = 0

    private val initialHolderState: HolderSessionState by lazy {
        HolderSessionState.Preflight(
            missingPrerequisites = listOf(
                MissingPrerequisites.Bluetooth(BluetoothState.PermissionNotGranted)
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
        FakeAnalyticsLogger()
    }

    private val resolver by lazy {
        ResolveHolderPrerequisiteAction(
            logger = logger,
            orchestrator = orchestrator,
            permissionChecker = FakePermissionChecker()
        )
    }

    private val viewModel by lazy {
        BluetoothPermissionDeniedViewModel(
            analyticsLogger = analyticsLogger,
            orchestrator = orchestrator,
            resolver = resolver,
            dispatcher = dispatcherRule.testDispatcher
        )
    }

    private val viewModelFactory = object : MetroViewModelFactory() {
        override val viewModelProviders: Map<KClass<out ViewModel>, () -> ViewModel>
            get() = mapOf(BluetoothPermissionDeniedViewModel::class to { viewModel })

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
            setContent {
                BluetoothPermissionDeniedContent(onAllow = {}, onCancel = {})
            }
            assertTitleIsDisplayed()
            assertBodyIsDisplayed()
            assertAllowButtonIsDisplayed()
            assertCancelButtonIsDisplayed()
        }
    }

    @Test
    fun `Tapping Allow launches the in-app Bluetooth permission prompt`() = runTest {
        composeTestRule.run {
            setContent {
                ProvideActivityResultRegistry(testRegistry) {
                    BluetoothPermissionDeniedScreen(viewModel = viewModel)
                }
            }

            performAllowClick()

            waitUntil { launchedAction != null }
            assertTrue(launchedAction is PrerequisiteAction.RequestPermissions)

            waitUntil { analyticsLogger.size >= 2 }
        }
        assertThat(
            analyticsLogger,
            contains(
                BluetoothPermissionDeniedViewModelTestData.expectedScreenEvent,
                BluetoothPermissionDeniedViewModelTestData.expectedAllowEvent
            )
        )
    }

    @Test
    fun `Tapping Cancel invokes the cancel callback and records analytics`() = runTest {
        var cancelled = false
        composeTestRule.run {
            setContent {
                ProvideActivityResultRegistry(testRegistry) {
                    BluetoothPermissionDeniedScreen(
                        viewModel = viewModel,
                        onCancel = { cancelled = true }
                    )
                }
            }

            performCancelClick()

            waitUntil { cancelled }
            waitUntil { analyticsLogger.size >= 2 }
        }
        assertTrue(cancelled)
        assertThat(
            analyticsLogger,
            contains(
                BluetoothPermissionDeniedViewModelTestData.expectedScreenEvent,
                BluetoothPermissionDeniedViewModelTestData.expectedCancelEvent
            )
        )
    }

    @Test
    fun `Prerequisites are rechecked after the permission prompt returns`() = runTest {
        composeTestRule.run {
            setContent {
                ProvideActivityResultRegistry(testRegistry) {
                    BluetoothPermissionDeniedScreen(viewModel = viewModel)
                }
            }

            performAllowClick()

            waitUntil("Prerequisites were not rechecked") {
                hasRecheckedPrerequisites
            }
        }
    }

    @Test
    fun `Displaying the screen sends a screen view event`() = runTest {
        composeTestRule.run {
            setContent { BluetoothPermissionDeniedScreen(viewModel = viewModel) }
            waitUntil { analyticsLogger.size != 0 }
        }
        assertThat(
            analyticsLogger,
            contains(BluetoothPermissionDeniedViewModelTestData.expectedScreenEvent)
        )
    }

    @Test
    @Config(qualifiers = "cy")
    fun `Displaying the screen in Welsh sends analytics events in English`() = runTest {
        composeTestRule.run {
            setContent {
                ProvideActivityResultRegistry(testRegistry) {
                    BluetoothPermissionDeniedScreen(viewModel = viewModel)
                }
            }

            assertWelshTitleIsDisplayed()

            performAllowClick()
            waitUntil { analyticsLogger.size >= 2 }
        }
        assertThat(
            analyticsLogger,
            contains(
                BluetoothPermissionDeniedViewModelTestData.expectedScreenEvent,
                BluetoothPermissionDeniedViewModelTestData.expectedAllowEvent
            )
        )
    }

    @Test
    fun `Closing the journey shows a loading spinner`() = runTest {
        composeTestRule.run {
            setContent { BluetoothPermissionDeniedScreen(viewModel = viewModel) }
            assertTitleIsDisplayed()
            holderState.update { HolderSessionState.SendingTermination }
            waitUntilLoadingSpinnerExists()
            assertLoadingSpinnerIsDisplayed()
            assertTitleDoesNotExist()
        }
    }

    @Test
    fun `Prerequisites are rechecked on resume after tapping Allow`() = runTest {
        val lifecycleOwner = TestLifecycleOwner(
            initialState = Lifecycle.State.RESUMED,
            coroutineDispatcher = Dispatchers.Unconfined
        )

        composeTestRule.run {
            setContent {
                CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                    ProvideActivityResultRegistry(testRegistry) {
                        BluetoothPermissionDeniedScreen(viewModel = viewModel)
                    }
                }
            }

            performAllowClick()
            waitUntil { recheckCount == 1 }

            runOnIdle {
                lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
                lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            }

            waitUntil { recheckCount == 2 }
        }
    }

    @Test
    fun `Prerequisites are not rechecked on resume before tapping Allow`() = runTest {
        val lifecycleOwner = TestLifecycleOwner(
            initialState = Lifecycle.State.RESUMED,
            coroutineDispatcher = Dispatchers.Unconfined
        )

        composeTestRule.run {
            setContent {
                CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                    ProvideActivityResultRegistry(testRegistry) {
                        BluetoothPermissionDeniedScreen(viewModel = viewModel)
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
                    BluetoothPermissionDeniedScreen()
                }
            }

            assertTitleIsDisplayed()
            waitUntil { analyticsLogger.size != 0 }
        }

        assertThat(
            analyticsLogger,
            contains(BluetoothPermissionDeniedViewModelTestData.expectedScreenEvent)
        )
    }

    @Test
    fun `Content preview renders the screen content`() = runTest {
        composeTestRule.run {
            setContent { BluetoothPermissionDeniedContentPreview() }
            assertTitleIsDisplayed()
            assertBodyIsDisplayed()
            assertAllowButtonIsDisplayed()
            assertCancelButtonIsDisplayed()
        }
    }

    @Test
    fun `Loading preview renders the loading content`() = runTest {
        composeTestRule.run {
            setContent { BluetoothPermissionDeniedLoadingContentPreview() }
            assertLoadingSpinnerIsDisplayed()
        }
    }
}
