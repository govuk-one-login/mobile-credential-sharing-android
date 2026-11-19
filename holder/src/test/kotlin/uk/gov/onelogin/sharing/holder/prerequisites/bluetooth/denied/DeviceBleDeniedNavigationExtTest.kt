package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth.denied

import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onParent
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.DialogNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import androidx.navigation.toRoute
import com.google.testing.junit.testparameterinjector.TestParameter
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.logging.analytics.FakeAnalyticsLogger
import uk.gov.logging.api.v3.MemorisedLogger
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.holder.prerequisites.bluetooth.denied.DeviceBleDeniedNavigationExt.configureDeviceBleDenied
import uk.gov.onelogin.sharing.holder.prerequisites.bluetooth.denied.DeviceBleDeniedNavigationExt.navigateToDeviceBleDenied
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.prerequisites.usecases.ResolveHolderPrerequisiteAction
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.prerequisites.api.state.BluetoothState
import uk.gov.onelogin.sharing.prerequisites.impl.MissingPrerequisites
import uk.gov.onelogin.sharing.prerequisites.permissions.FakePermissionChecker

@RunWith(RobolectricTestParameterInjector::class)
class DeviceBleDeniedNavigationExtTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = DeviceBleDeniedScreenRule()

    private lateinit var controller: TestNavHostController

    private var hasNavigatedToDeviceBleDenied = false

    private val initialHolderState: HolderSessionState by lazy {
        HolderSessionState.Preflight(
            missingPrerequisites = listOf(
                MissingPrerequisites.Bluetooth(BluetoothState.PoweredOff)
            ),
            enableBluetoothDenied = true
        ) {}
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
        DeviceBleDeniedViewModel(
            analyticsLogger = analyticsLogger,
            orchestrator = orchestrator,
            resolver = resolver,
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

    private val onDestinationChangedListener = NavController.OnDestinationChangedListener {
            controller,
            _,
            _
        ->

        try {
            controller.currentBackStackEntry?.toRoute<DeviceBleDeniedRoute>()
            hasNavigatedToDeviceBleDenied = true
        } catch (ignored: Exception) {
        }
    }

    @Test
    fun `Device Bluetooth denied screen exists in the navigation graph`() = runTest {
        composeTestRule.run {
            setNavContent()
            navigateToDeviceBleDenied()
            assertValidUi()
        }
    }

    @Test
    fun `Popping the back stack leaves the screen`() = runTest {
        composeTestRule.run {
            setNavContent()
            navigateToDeviceBleDenied()
            assertValidUi()

            runOnUiThread { controller.popBackStack() }

            waitUntil("Did not leave the device Bluetooth denied screen") {
                controller.currentBackStackEntry?.destination?.route == START_DESTINATION
            }
            assertTitleDoesNotExist()
        }
    }

    @Test
    fun `Every way of cancelling the journey cancels it via the orchestrator`(
        @TestParameter action: CancelAction
    ) = runTest {
        composeTestRule.run {
            val backPressedDispatcherOwner = setNavContent()
            navigateToDeviceBleDenied()
            assertValidUi()

            when (action) {
                CancelAction.CancelButton -> performCancelClick()

                CancelAction.BackPressedDispatcher -> runOnUiThread {
                    backPressedDispatcherOwner()?.onBackPressedDispatcher?.onBackPressed()
                }
            }

            waitUntil("Did not cancel the journey via $action") {
                orchestrator.cancelCount == 1
            }
            assertEquals(0, orchestrator.resetCount)
        }
    }

    enum class CancelAction {
        CancelButton,
        BackPressedDispatcher
    }

    private fun DeviceBleDeniedScreenRule.setNavContent(): () -> OnBackPressedDispatcherOwner? {
        var owner: OnBackPressedDispatcherOwner? = null
        setContent {
            owner = LocalOnBackPressedDispatcherOwner.current
            controller = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
                navigatorProvider.addNavigator(DialogNavigator())
                addOnDestinationChangedListener(onDestinationChangedListener)
            }

            CompositionLocalProvider(LocalMetroViewModelFactory provides viewModelFactory) {
                Render()
            }
        }
        return { owner }
    }

    private fun DeviceBleDeniedScreenRule.navigateToDeviceBleDenied() {
        onNodeWithText("Navigate").onParent().performClick()
        waitUntil { hasNavigatedToDeviceBleDenied }
        waitForIdle()
    }

    @Composable
    private fun Render() {
        NavHost(
            navController = controller,
            startDestination = START_DESTINATION
        ) {
            composable(START_DESTINATION) {
                Button(
                    onClick = {
                        controller.navigateToDeviceBleDenied()
                    }
                ) {
                    Text("Navigate")
                }
            }

            configureDeviceBleDenied()
        }
    }

    private companion object {
        const val START_DESTINATION = "Unit test"
    }
}
