package uk.gov.onelogin.sharing.orchestration.holder.session

import java.util.UUID
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc.FakePeripheralBluetoothTransport
import uk.gov.onelogin.sharing.core.MainDispatcherRule

@OptIn(ExperimentalCoroutinesApi::class)
class HolderSessionTerminatorImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeTransport = FakePeripheralBluetoothTransport()
    private val logger = SystemLogger()

    private lateinit var terminator: HolderSessionTerminatorImpl

    private val serviceUuid: UUID = UUID.randomUUID()

    @Before
    fun setUp() {
        terminator = HolderSessionTerminatorImpl(fakeTransport, logger)
    }

    @Test
    fun `terminate notifies session end and stops peripheral`() = runTest {
        terminator.terminate(serviceUuid)

        assertEquals(serviceUuid, fakeTransport.lastUuid)
        assertEquals(1, fakeTransport.stopCalls)
    }

    @Test
    fun `GATT End is not sent before 500ms delay elapses`() = runTest {
        val job = launch { terminator.terminate(serviceUuid) }

        advanceTimeBy((HolderSessionTerminatorImpl.TERMINATION_DELAY_MS - 1).milliseconds)
        assertEquals(0, fakeTransport.stopCalls)

        advanceTimeBy(2.milliseconds)
        job.join()

        assertEquals(1, fakeTransport.stopCalls)
    }

    @Test
    fun `logs are emitted during termination`() = runTest {
        terminator.terminate(serviceUuid)

        assert(
            "Waiting ${HolderSessionTerminatorImpl.TERMINATION_DELAY_MS}ms " +
                "before sending GATT End" in logger
        )
        assert("Sending GATT End (0x02) to verifier" in logger)
        assert("Stopping BLE peripheral" in logger)
        assert("Holder session terminated" in logger)
    }
}
