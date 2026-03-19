package uk.gov.onelogin.sharing.verifier.session

import app.cash.turbine.test
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.google.testing.junit.testparameterinjector.TestParameters
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.api.gatt.central.GattClientEvent
import uk.gov.onelogin.sharing.bluetooth.ble.DEVICE_ADDRESS
import uk.gov.onelogin.sharing.bluetooth.ble.FakeBluetoothStateMonitor
import uk.gov.onelogin.sharing.bluetooth.internal.central.FakeGattClientManager
import uk.gov.onelogin.sharing.bluetooth.internal.core.SessionEndStates
import uk.gov.onelogin.sharing.core.MainDispatcherRule

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(TestParameterInjector::class)
class MdocVerifierSessionTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val gattClientManager = FakeGattClientManager()
    private val bluetoothStateMonitor = FakeBluetoothStateMonitor()
    private val logger = SystemLogger()

    private lateinit var session: MdocVerifierSession

    @Before
    fun setUp() {
        session = MdocVerifierSession(
            gattClientManager = gattClientManager,
            bluetoothStateMonitor = bluetoothStateMonitor,
            logger = logger,
            scope = TestScope(StandardTestDispatcher())
        )
    }

    @Test
    fun `start logs Starting session`() = runTest {
        session.start(UUID.randomUUID())

        assertTrue(logger.contains("Starting session"))

        session.state.test {
            assertEquals(VerifierSessionState.Starting, awaitItem())
        }
    }

    @Test
    @TestParameters(valuesProvider = GattClientEventsToVerifierSessionStates::class)
    fun `Converts Gatt events to session state events`(
        input: GattClientEvent,
        expectedState: VerifierSessionState
    ) = runTest {
        gattClientManager.emitEvent(input)

        advanceUntilIdle()

        session.state.test {
            val sessionState = awaitItem()
            assertEquals(
                "Session state doesn't match",
                expectedState,
                sessionState
            )
        }
    }

    @Test
    fun `non-ServicesDiscovered event logs Unhandled event`() = GattClientEvent.UnsupportedEvent(
        DEVICE_ADDRESS,
        status = 999,
        newState = 999
    ).let { event ->
        `Converts Gatt events to session state events`(
            input = event,
            expectedState = VerifierSessionState.Error(
                "Unhandled event: $event"
            )
        )
        assertTrue("Unhandled event: $event" in logger)
    }

    @Test
    fun `sessionEnd event disconnects gatt client manager`() = runTest {
        gattClientManager.emitEvent(GattClientEvent.SessionEnd(SessionEndStates.SUCCESS))
        advanceUntilIdle()
        assertEquals(1, gattClientManager.disconnectCalls)
    }
}
