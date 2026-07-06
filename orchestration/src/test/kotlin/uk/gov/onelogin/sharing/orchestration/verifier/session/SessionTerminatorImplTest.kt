package uk.gov.onelogin.sharing.orchestration.verifier.session

import app.cash.turbine.test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.api.central.mdoc.FakeCentralBluetoothTransport
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.cryptoService.verifier.FakeVerifierCryptoService

@OptIn(ExperimentalCoroutinesApi::class)
class SessionTerminatorImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeTransport = FakeCentralBluetoothTransport()
    private val fakeCryptoService = FakeVerifierCryptoService()
    private val logger = SystemLogger()

    private lateinit var terminator: SessionTerminatorImpl

    private val serviceUuid: UUID = UUID.randomUUID()

    @Before
    fun setUp() {
        terminator = SessionTerminatorImpl(fakeTransport, fakeCryptoService, logger)
    }

    @Test
    fun `BLE open, holder did not send status 20 - sends termination then GATT End then stops`() =
        runTest {
            terminator.terminate(
                serviceUuid = serviceUuid,
                bleOpen = true,
                holderRequestedTermination = false
            )

            assertEquals(1, fakeCryptoService.buildTerminationSessionDataCalls)
            assertArrayEquals(
                fakeCryptoService.buildTerminationSessionData(),
                fakeTransport.lastSentData
            )

            assertEquals(1, fakeTransport.sendEndCalls)
            assertEquals(1, fakeTransport.stopCalls)
            assertEquals(TerminationState.TERMINATED, terminator.state.value)
        }

    @Test
    fun `state machine emits correct sequence when BLE open and holder did not send status 20`() =
        runTest {
            terminator.state.test {
                assertEquals(TerminationState.IDLE, awaitItem())

                val job = launch {
                    terminator.terminate(
                        serviceUuid = serviceUuid,
                        bleOpen = true,
                        holderRequestedTermination = false
                    )
                }

                assertEquals(TerminationState.SENDING_TERMINATION, awaitItem())
                assertEquals(TerminationState.AWAITING_DELAY, awaitItem())

                advanceTimeBy((SessionTerminatorImpl.TERMINATION_DELAY_MS + 1).milliseconds)

                assertEquals(TerminationState.SENDING_GATT_END, awaitItem())
                assertEquals(TerminationState.STOPPING, awaitItem())
                assertEquals(TerminationState.TERMINATED, awaitItem())

                job.join()
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `GATT End is not sent before 500ms delay elapses`() = runTest {
        val job = launch {
            terminator.terminate(
                serviceUuid = serviceUuid,
                bleOpen = true,
                holderRequestedTermination = false
            )
        }

        advanceTimeBy((SessionTerminatorImpl.TERMINATION_DELAY_MS - 1).milliseconds)
        assertEquals(0, fakeTransport.sendEndCalls)
        advanceTimeBy(2.milliseconds)
        job.join()
        assertEquals(1, fakeTransport.sendEndCalls)
    }

    @Test
    fun `BLE open, holder sent status 20, skips termination message, sends GATT End then stops`() =
        runTest {
            terminator.terminate(
                serviceUuid = serviceUuid,
                bleOpen = true,
                holderRequestedTermination = true
            )

            assertEquals(0, fakeCryptoService.buildTerminationSessionDataCalls)
            assertEquals(1, fakeTransport.sendEndCalls)
            assertEquals(1, fakeTransport.stopCalls)
            assertEquals(TerminationState.TERMINATED, terminator.state.value)
        }

    @Test
    fun `state machine jumps directly to SENDING_END when holder sent status 20 and BLE open`() =
        runTest {
            terminator.state.test {
                assertEquals(TerminationState.IDLE, awaitItem())

                val job = launch {
                    terminator.terminate(
                        serviceUuid = serviceUuid,
                        bleOpen = true,
                        holderRequestedTermination = true
                    )
                }

                assertEquals(TerminationState.SENDING_GATT_END, awaitItem())
                assertEquals(TerminationState.STOPPING, awaitItem())
                assertEquals(TerminationState.TERMINATED, awaitItem())

                job.join()
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `BLE closed - no SessionData sent, no GATT End, no stop`() = runTest {
        terminator.terminate(
            serviceUuid = serviceUuid,
            bleOpen = false,
            holderRequestedTermination = true
        )

        assertEquals(0, fakeCryptoService.buildTerminationSessionDataCalls)
        assertEquals(0, fakeTransport.sendEndCalls)
        assertEquals(0, fakeTransport.stopCalls)

        assertEquals(TerminationState.TERMINATED, terminator.state.value)
    }

    @Test
    fun `state machine transitions directly to TERMINATED when BLE closed`() = runTest {
        terminator.state.test {
            assertEquals(TerminationState.IDLE, awaitItem())

            val job = launch {
                terminator.terminate(
                    serviceUuid = serviceUuid,
                    bleOpen = false,
                    holderRequestedTermination = true
                )
            }

            assertEquals(TerminationState.TERMINATED, awaitItem())

            job.join()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `null serviceUuid with BLE closed still terminates cleanly`() = runTest {
        terminator.terminate(
            serviceUuid = null,
            bleOpen = false,
            holderRequestedTermination = true
        )

        assertEquals(0, fakeCryptoService.buildTerminationSessionDataCalls)
        assertEquals(0, fakeTransport.sendEndCalls)
        assertEquals(0, fakeTransport.stopCalls)
        assertEquals(TerminationState.TERMINATED, terminator.state.value)
    }

    @Test
    fun `null serviceUuid, BLE open skips termination message, sends GATT End and stops`() =
        runTest {
            terminator.terminate(
                serviceUuid = null,
                bleOpen = true,
                holderRequestedTermination = false
            )

            assertEquals(0, fakeCryptoService.buildTerminationSessionDataCalls)
            assertEquals(1, fakeTransport.sendEndCalls)
            assertEquals(1, fakeTransport.stopCalls)

            assertEquals(TerminationState.TERMINATED, terminator.state.value)
        }
}
