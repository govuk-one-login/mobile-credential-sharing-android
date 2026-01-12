package uk.gov.onelogin.sharing.verifier.session

import android.bluetooth.BluetoothDevice
import app.cash.turbine.test
import io.mockk.mockk
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
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.api.gatt.central.ClientError
import uk.gov.onelogin.sharing.bluetooth.api.gatt.central.GattClientEvent
import uk.gov.onelogin.sharing.bluetooth.ble.DEVICE_ADDRESS
import uk.gov.onelogin.sharing.bluetooth.ble.FakeBluetoothStateMonitor
import uk.gov.onelogin.sharing.bluetooth.internal.central.FakeGattClientManager
import uk.gov.onelogin.sharing.core.MainDispatcherRule

@OptIn(ExperimentalCoroutinesApi::class)
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
    fun `stop logs Stop session`() = runTest {
        session.stop()

        assertTrue(logger.contains("Stop session"))

        session.state.test {
            assertEquals(VerifierSessionState.Stopped, awaitItem())
        }
    }

    @Test
    fun `Connecting triggers Connecting verifier state`() = runTest {
        gattClientManager.emitEvent(GattClientEvent.Connecting)

        advanceUntilIdle()

        session.state.test {
            assertEquals(VerifierSessionState.Connecting, awaitItem())
        }
    }

    @Test
    fun `Connected triggers Connected verifier state`() = runTest {
        val device = mockk<BluetoothDevice>(relaxed = true)

        gattClientManager.emitEvent(
            GattClientEvent.Connected(device.address)
        )

        advanceUntilIdle()

        session.state.test {
            assertEquals(
                VerifierSessionState.Connected(device.address),
                awaitItem()
            )
        }
    }

    @Test
    fun `Disconnected triggers Disconnected verifier state`() = runTest {
        val device = mockk<BluetoothDevice>(relaxed = true)

        gattClientManager.emitEvent(
            GattClientEvent.Disconnected(device.address)
        )

        advanceUntilIdle()

        session.state.test {
            assertEquals(
                VerifierSessionState.Disconnected(device.address),
                awaitItem()
            )
        }
    }

    @Test
    fun `Gatt client Error triggers Error verifier state`() = runTest {
        gattClientManager.emitEvent(
            GattClientEvent.Error(ClientError.BLUETOOTH_PERMISSION_MISSING)
        )

        advanceUntilIdle()

        session.state.test {
            assertEquals(
                VerifierSessionState.Error(
                    ClientError.BLUETOOTH_PERMISSION_MISSING.toString()
                ),
                awaitItem()
            )
        }
    }

    @Test
    fun `non-ServicesDiscovered event logs Unhandled event`() = runTest {
        val event = GattClientEvent.UnsupportedEvent(
            DEVICE_ADDRESS,
            status = 999,
            newState = 999
        )

        gattClientManager.emitEvent(event)

        advanceUntilIdle()

        assertTrue(logger.contains("Unhandled event: $event"))

        session.state.test {
            assertEquals(
                VerifierSessionState.Error(
                    "Unhandled event: $event"
                ),
                awaitItem()
            )
        }
    }
}
