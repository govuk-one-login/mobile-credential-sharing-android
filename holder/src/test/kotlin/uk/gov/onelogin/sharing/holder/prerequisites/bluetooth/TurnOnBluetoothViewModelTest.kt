package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityOptionsCompat
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
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.analytics.StoredAnalyticsLogger
import uk.gov.onelogin.sharing.analytics.StoredAnalyticsLoggerMatchers.hasSentEvents
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.prerequisites.usecases.ResolveHolderPrerequisiteAction
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.prerequisites.api.PrerequisiteAction
import uk.gov.onelogin.sharing.prerequisites.api.contracts.PrerequisiteActionContract
import uk.gov.onelogin.sharing.prerequisites.api.state.BluetoothState
import uk.gov.onelogin.sharing.prerequisites.impl.MissingPrerequisites
import uk.gov.onelogin.sharing.prerequisites.permissions.FakePermissionChecker

class TurnOnBluetoothViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val analyticsLogger = StoredAnalyticsLogger()
    private val logger = SystemLogger()
    private var recheckCount = 0
    private val holderState = MutableStateFlow<HolderSessionState>(
        HolderSessionState.Preflight(
            listOf(MissingPrerequisites.Bluetooth(BluetoothState.PoweredOff))
        ) { recheckCount++ }
    )
    private val orchestrator = FakeOrchestrator(
        initialHolderState = holderState
    )

    private val resolver = ResolveHolderPrerequisiteAction(
        logger = logger,
        orchestrator = orchestrator,
        permissionChecker = FakePermissionChecker()
    )

    private val viewModel by lazy {
        TurnOnBluetoothViewModel(
            analyticsLogger = analyticsLogger,
            orchestrator = orchestrator,
            resolver = resolver,
            dispatcher = dispatcherRule.testDispatcher
        )
    }

    @Test
    fun `Logs screen view event`() = runTest {
        viewModel.sendScreenEvent().join()
        assertThat(
            analyticsLogger,
            hasSentEvents(contains(TurnOnBluetoothViewModelTestData.expectedScreenEvent))
        )
    }

    @Test
    fun `Logs Turn on Bluetooth button event`() = runTest {
        viewModel.sendTurnOnBluetoothEvent(
            TurnOnBluetoothViewModel.AnalyticsButton.TurnOnBluetooth
        ).join()
        assertThat(
            analyticsLogger,
            hasSentEvents(contains(TurnOnBluetoothViewModelTestData.expectedTurnOnBluetoothEvent))
        )
    }

    @Test
    fun `Turning on Bluetooth logs the button event and requests Bluetooth is enabled`() {
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
        assertThat(
            analyticsLogger,
            hasSentEvents(contains(TurnOnBluetoothViewModelTestData.expectedTurnOnBluetoothEvent))
        )
    }

    @Test
    fun `Rechecking prerequisites re-runs Preflight checks`() = runTest {
        assertFalse(viewModel.hasRecheckedPrerequisites.value)
        viewModel.recheckPrerequisites().join()
        assertEquals(1, recheckCount)
        assertTrue(viewModel.hasRecheckedPrerequisites.value)
    }

    @Test
    fun `Rechecking prerequisites outside Preflight does not re-run checks`() = runTest {
        holderState.update { HolderSessionState.SendingTermination }
        viewModel.recheckPrerequisites().join()
        assertEquals(0, recheckCount)
    }

    @Test
    fun `isClosing is true once session leaves Preflight`() {
        assertFalse(viewModel.isClosing.value)
        holderState.update { HolderSessionState.SendingTermination }
        assertTrue(viewModel.isClosing.value)
    }
}
