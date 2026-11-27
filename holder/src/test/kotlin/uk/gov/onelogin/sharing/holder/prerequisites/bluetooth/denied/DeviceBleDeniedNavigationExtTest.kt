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
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import androidx.navigation.toRoute
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterValuesProvider
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.logging.analytics.FakeAnalyticsLogger
import uk.gov.logging.api.v3.MemorisedLogger
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.holder.TestViewModelFactory
import uk.gov.onelogin.sharing.holder.navigation.TestNavHostControllerHelper.prepareController
import uk.gov.onelogin.sharing.holder.prerequisites.bluetooth.denied.DeviceBleDeniedNavigationExt.configureDeviceBleDenied
import uk.gov.onelogin.sharing.holder.prerequisites.bluetooth.denied.DeviceBleDeniedNavigationExt.navigateToDeviceBleDenied
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.FakeOrchestratorMatchers.hasCancelCount
import uk.gov.onelogin.sharing.orchestration.FakeOrchestratorMatchers.hasResetCount
import uk.gov.onelogin.sharing.orchestration.holder.prerequisites.usecases.ResolveHolderPrerequisiteAction
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.prerequisites.api.state.BluetoothState
import uk.gov.onelogin.sharing.prerequisites.impl.MissingPrerequisites
import uk.gov.onelogin.sharing.prerequisites.permissions.FakePermissionChecker

private const val START_DESTINATION = "Unit test"

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

    private val viewModelFactory = TestViewModelFactory(
        viewModelProviders = mapOf(
            DeviceBleDeniedViewModel::class to { viewModel }
        )
    )

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
    fun `Every way of leaving the screen pops it off the back stack`(
        @TestParameter(valuesProvider = LeaveActionProvider::class) action: LeaveAction
    ) = runTest {
        composeTestRule.run {
            val backPressedDispatcherOwner = setNavContent()
            navigateToDeviceBleDenied()
            assertValidUi()

            action.perform(this, controller, backPressedDispatcherOwner)

            waitUntil("Did not leave the device Bluetooth denied screen via $action") {
                controller.currentBackStackEntry?.destination?.route == START_DESTINATION
            }
            assertTitleDoesNotExist()

            assertThat(
                orchestrator,
                allOf(
                    hasCancelCount(0),
                    hasResetCount(0)
                )
            )
        }
    }

    private fun DeviceBleDeniedScreenRule.setNavContent(): () -> OnBackPressedDispatcherOwner? {
        var owner: OnBackPressedDispatcherOwner? = null
        setContent {
            owner = LocalOnBackPressedDispatcherOwner.current
            controller = prepareController(LocalContext.current).apply {
                addOnDestinationChangedListener(onDestinationChangedListener)
            }

            Render()
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
        CompositionLocalProvider(
            LocalMetroViewModelFactory provides viewModelFactory
        ) {
            GdsTheme {
                NavHost(
                    navController = controller,
                    startDestination = START_DESTINATION
                ) {
                    startRouteButton {
                        controller.navigateToDeviceBleDenied()
                    }

                    configureDeviceBleDenied()
                }
            }
        }
    }

    data class LeaveAction(
        val description: String,
        val perform: DeviceBleDeniedScreenRule.(
            controller: TestNavHostController,
            backPressedDispatcherOwner: () -> OnBackPressedDispatcherOwner?
        ) -> Unit
    ) {
        override fun toString(): String = description
    }

    class LeaveActionProvider : TestParameterValuesProvider() {
        override fun provideValues(context: Context?): List<LeaveAction> = listOf(
            LeaveAction(description = "Cancel button") { _, _ -> performCancelClick() },
            LeaveAction(description = "Pop back stack") { controller, _ ->
                runOnUiThread { controller.popBackStack() }
            },
            LeaveAction(description = "Back pressed dispatcher") { _, owner ->
                runOnUiThread { owner()?.onBackPressedDispatcher?.onBackPressed() }
            }
        )
    }
}

private fun NavGraphBuilder.startRouteButton(onClick: () -> Unit) {
    composable(route = START_DESTINATION) {
        Button(onClick = onClick) {
            Text("Navigate")
        }
    }
}
