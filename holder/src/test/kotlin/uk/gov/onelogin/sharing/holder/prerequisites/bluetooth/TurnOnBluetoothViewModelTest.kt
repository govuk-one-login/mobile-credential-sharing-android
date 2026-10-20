package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth

import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow
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
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionStateStubs
import uk.gov.onelogin.sharing.prerequisites.permissions.FakePermissionChecker

class TurnOnBluetoothViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val analyticsLogger = StoredAnalyticsLogger()
    private val logger = SystemLogger()
    private val orchestrator = FakeOrchestrator(
        initialHolderState = MutableStateFlow(HolderSessionStateStubs.preflightWithoutBluetooth)
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
}
