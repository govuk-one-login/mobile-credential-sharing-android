package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth

import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityOptionsCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
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
import org.robolectric.annotation.Config
import uk.gov.logging.api.v3.MemorisedLogger
import uk.gov.onelogin.sharing.analytics.StoredAnalyticsLogger
import uk.gov.onelogin.sharing.analytics.StoredAnalyticsLoggerMatchers.hasSentEvents
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.holder.prerequisites.bluetooth.TurnOnBluetoothViewModelTestData.BUTTON_TEXT
import uk.gov.onelogin.sharing.holder.prerequisites.bluetooth.TurnOnBluetoothViewModelTestData.SCREEN_ID
import uk.gov.onelogin.sharing.holder.prerequisites.bluetooth.TurnOnBluetoothViewModelTestData.SCREEN_NAME
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.prerequisites.usecases.ResolveHolderPrerequisiteAction
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.prerequisites.api.PrerequisiteAction
import uk.gov.onelogin.sharing.prerequisites.api.contracts.PrerequisiteActionContract
import uk.gov.onelogin.sharing.prerequisites.api.state.BluetoothState
import uk.gov.onelogin.sharing.prerequisites.impl.MissingPrerequisites
import uk.gov.onelogin.sharing.prerequisites.permissions.FakePermissionChecker

@RunWith(AndroidJUnit4::class)
class TurnOnBluetoothViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val context: Context by lazy { ApplicationProvider.getApplicationContext() }
    private val analyticsLogger = StoredAnalyticsLogger()
    private val logger = MemorisedLogger()
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
        viewModel.sendScreenEvent(context).join()
        assertThat(
            analyticsLogger,
            hasSentEvents(contains(TurnOnBluetoothViewModelTestData.expectedScreenEvent))
        )
    }

    @Test
    fun `Logs Turn on Bluetooth button event`() = runTest {
        viewModel.sendTurnOnBluetoothEvent(context).join()
        assertThat(
            analyticsLogger,
            hasSentEvents(contains(TurnOnBluetoothViewModelTestData.expectedTurnOnBluetoothEvent))
        )
    }

    @Test
    @Config(qualifiers = "cy")
    fun `Screen view event is in English when the device is in Welsh`() = runTest {
        viewModel.sendScreenEvent(context).join()

        val event = analyticsLogger.sentAnalyticsEvents.single()
        assertEquals(SCREEN_NAME.lowercase(), event.parameters["screen_name"])
        assertEquals("cy", event.parameters["language"])
    }

    @Test
    @Config(qualifiers = "cy")
    fun `Turn on Bluetooth button event is in English when device is in Welsh`() = runTest {
        viewModel.sendTurnOnBluetoothEvent(context).join()

        val event = analyticsLogger.sentAnalyticsEvents.single()
        assertEquals(BUTTON_TEXT.lowercase(), event.parameters["text"])
        assertEquals("cy", event.parameters["language"])
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
    fun `Screen view event sets screen class, name, and id`() = runTest {
        viewModel.sendScreenEvent(context).join()

        val event = analyticsLogger.sentAnalyticsEvents.single()
        assertEquals("screen_view", event.eventType)
        assertEquals(SCREEN_NAME.lowercase(), event.parameters["screen_name"])
        assertEquals(SCREEN_ID, event.parameters["screen_id"])
    }

    @Test
    fun `Turn on Bluetooth button event sets text and type`() = runTest {
        viewModel.sendTurnOnBluetoothEvent(context).join()

        val event = analyticsLogger.sentAnalyticsEvents.single()
        assertEquals("navigation", event.eventType)
        assertEquals(BUTTON_TEXT.lowercase(), event.parameters["text"])
        assertEquals("submit form", event.parameters["type"])
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
