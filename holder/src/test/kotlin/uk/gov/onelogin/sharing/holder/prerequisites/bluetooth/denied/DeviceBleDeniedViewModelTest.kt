package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth.denied

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityOptionsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.junit.Rule
import org.junit.runner.RunWith
import uk.gov.logging.api.v3.MemorisedLogger
import uk.gov.onelogin.sharing.analytics.StoredAnalyticsLogger
import uk.gov.onelogin.sharing.analytics.StoredAnalyticsLoggerMatchers.hasSentEvents
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.holder.prerequisites.bluetooth.denied.DeviceBleDeniedTestData.CANCEL_BUTTON_TEXT
import uk.gov.onelogin.sharing.holder.prerequisites.bluetooth.denied.DeviceBleDeniedTestData.SCREEN_ID
import uk.gov.onelogin.sharing.holder.prerequisites.bluetooth.denied.DeviceBleDeniedTestData.SCREEN_NAME
import uk.gov.onelogin.sharing.holder.prerequisites.bluetooth.denied.DeviceBleDeniedTestData.TURN_ON_BUTTON_TEXT
import uk.gov.onelogin.sharing.holder.prerequisites.bluetooth.denied.DeviceBleDeniedViewModel.NavigationEvent
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.prerequisites.usecases.ResolveHolderPrerequisiteAction
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.prerequisites.api.PrerequisiteAction
import uk.gov.onelogin.sharing.prerequisites.api.contracts.PrerequisiteActionContract
import uk.gov.onelogin.sharing.prerequisites.api.state.BluetoothState
import uk.gov.onelogin.sharing.prerequisites.impl.MissingPrerequisites
import uk.gov.onelogin.sharing.prerequisites.permissions.FakePermissionChecker

@RunWith(AndroidJUnit4::class)
class DeviceBleDeniedViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val analyticsLogger = StoredAnalyticsLogger()
    private var recheckCount = 0
    private val holderState = MutableStateFlow<HolderSessionState>(
        HolderSessionState.Preflight(
            missingPrerequisites = listOf(
                MissingPrerequisites.Bluetooth(BluetoothState.PoweredOff)
            ),
            enableBluetoothDenied = true
        ) { recheckCount++ }
    )
    private val orchestrator = FakeOrchestrator(initialHolderState = holderState)

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

    @Test
    fun `Screen view event sets screen name and id`() = runTest {
        viewModel.sendScreenEvent(SCREEN_NAME).join()

        assertThat(
            analyticsLogger,
            hasSentEvents(contains(DeviceBleDeniedTestData.expectedScreenEvent))
        )
        val event = analyticsLogger.sentAnalyticsEvents.single()
        assertEquals("screen_view", event.eventType)
        assertEquals(SCREEN_NAME.lowercase(), event.parameters["screen_name"])
        assertEquals(SCREEN_ID, event.parameters["screen_id"])
    }

    @Test
    fun `Logs Turn on Bluetooth button event`() = runTest {
        viewModel.sendTurnOnBluetoothEvent(TURN_ON_BUTTON_TEXT).join()

        assertThat(
            analyticsLogger,
            hasSentEvents(contains(DeviceBleDeniedTestData.expectedTurnOnBluetoothEvent))
        )
    }

    @Test
    fun `Logs Cancel button event`() = runTest {
        viewModel.sendCancelEvent(CANCEL_BUTTON_TEXT).join()

        assertThat(
            analyticsLogger,
            hasSentEvents(contains(DeviceBleDeniedTestData.expectedCancelEvent))
        )
    }

    @Test
    fun `Turning on Bluetooth requests Bluetooth is enabled`() {
        var launchedAction: PrerequisiteAction? = null
        val launcher = object : ActivityResultLauncher<PrerequisiteAction>() {
            override val contract: ActivityResultContract<PrerequisiteAction, *>
                get() = PrerequisiteActionContract

            override fun launch(input: PrerequisiteAction, options: ActivityOptionsCompat?) {
                launchedAction = input
            }

            override fun unregister() = Unit
        }

        viewModel.turnOnBluetooth(launcher)

        assertEquals(PrerequisiteAction.EnableBluetooth, launchedAction)
    }

    @Test
    fun `Rechecking prerequisites re-runs Preflight checks`() = runTest {
        viewModel.recheckPrerequisites().join()
        assertEquals(1, recheckCount)
    }

    @Test
    fun `Rechecking prerequisites outside Preflight does not re-run checks`() = runTest {
        holderState.update { HolderSessionState.NotStarted }
        viewModel.recheckPrerequisites().join()
        assertEquals(0, recheckCount)
    }

    @Test
    fun `Exiting the journey resets the orchestrator and emits an exit event`() = runTest {
        viewModel.navigationEvent.test {
            viewModel.exitJourney()
            assertEquals(NavigationEvent.ExitJourney, awaitItem())
        }
        assertEquals(1, orchestrator.resetCount)
        assertEquals(0, orchestrator.cancelCount)
    }

    @Test
    fun `isClosing is true once session leaves Preflight`() {
        assertFalse(viewModel.isClosing.value)
        holderState.update { HolderSessionState.NotStarted }
        assertTrue(viewModel.isClosing.value)
    }
}
