package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth.permission

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
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.runner.RunWith
import uk.gov.logging.analytics.FakeAnalyticsLogger
import uk.gov.logging.api.v3.MemorisedLogger
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.holder.prerequisites.bluetooth.permission.BluetoothPermissionDeniedNavigationExt.configureBluetoothPermissionDenied
import uk.gov.onelogin.sharing.holder.prerequisites.bluetooth.permission.BluetoothPermissionDeniedNavigationExt.navigateToBluetoothPermissionDenied
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.prerequisites.usecases.ResolveHolderPrerequisiteAction
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.prerequisites.api.state.BluetoothState
import uk.gov.onelogin.sharing.prerequisites.impl.MissingPrerequisites
import uk.gov.onelogin.sharing.prerequisites.permissions.FakePermissionChecker

@RunWith(AndroidJUnit4::class)
class BluetoothPermissionDeniedNavigationExtTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = BluetoothPermissionDeniedScreenRule()

    private lateinit var controller: TestNavHostController

    private var hasNavigatedToBluetoothPermissionDenied = false

    private val initialHolderState: HolderSessionState by lazy {
        HolderSessionState.Preflight(
            missingPrerequisites = listOf(
                MissingPrerequisites.Bluetooth(BluetoothState.PermissionNotGranted)
            )
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

    private val onDestinationChangedListener = NavController.OnDestinationChangedListener {
            controller,
            _,
            _
        ->

        try {
            controller.currentBackStackEntry?.toRoute<BluetoothPermissionDeniedRoute>()
            hasNavigatedToBluetoothPermissionDenied = true
        } catch (ignored: Exception) {
        }
    }

    @Test
    fun `Bluetooth permission denied screen exists in the navigation graph`() = runTest {
        composeTestRule.run {
            setContent {
                controller = TestNavHostController(LocalContext.current).apply {
                    navigatorProvider.addNavigator(ComposeNavigator())
                    navigatorProvider.addNavigator(DialogNavigator())
                    addOnDestinationChangedListener(onDestinationChangedListener)
                }

                CompositionLocalProvider(LocalMetroViewModelFactory provides viewModelFactory) {
                    Render()
                }
            }

            onNodeWithText("Navigate").onParent().performClick()

            waitUntil { hasNavigatedToBluetoothPermissionDenied }

            assertValidUi()
        }
    }

    @Composable
    private fun Render() {
        NavHost(
            navController = controller,
            startDestination = "Unit test"
        ) {
            composable("Unit test") {
                Button(
                    onClick = {
                        controller.navigateToBluetoothPermissionDenied()
                    }
                ) {
                    Text("Navigate")
                }
            }

            configureBluetoothPermissionDenied()
        }
    }
}
