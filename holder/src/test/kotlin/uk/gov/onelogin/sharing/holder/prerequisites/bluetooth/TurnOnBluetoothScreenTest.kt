package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth

import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.core.app.ActivityOptionsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.analytics.StoredAnalyticsLogger
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.core.activity.registry.ActivityResultLauncherExt.ProvideActivityResultRegistry
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.prerequisites.usecases.ResolveHolderPrerequisiteAction
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.prerequisites.api.PrerequisiteAction
import uk.gov.onelogin.sharing.prerequisites.api.state.BluetoothState
import uk.gov.onelogin.sharing.prerequisites.impl.MissingPrerequisites
import uk.gov.onelogin.sharing.prerequisites.permissions.FakePermissionChecker
import kotlin.getValue
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class TurnOnBluetoothScreenTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = TurnOnBluetoothScreenRule(createComposeRule())

    private var hasRecheckedPrerequisites = false

    private val initialHolderState: HolderSessionState by lazy {
        HolderSessionState.Preflight(
            missingPrerequisites = listOf(
                MissingPrerequisites.Bluetooth(BluetoothState.PoweredOff)
            )
        ) {
            hasRecheckedPrerequisites = true
        }
    }
    private val orchestrator by lazy {
        FakeOrchestrator(
            initialHolderState = MutableStateFlow(initialHolderState)
        )
    }

    private val logger = SystemLogger()

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
}
