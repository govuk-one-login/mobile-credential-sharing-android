package uk.gov.onelogin.sharing.bluetooth.internal

import app.cash.turbine.test
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import uk.gov.onelogin.sharing.bluetooth.api.MdocError
import uk.gov.onelogin.sharing.bluetooth.api.MdocSessionState
import uk.gov.onelogin.sharing.bluetooth.ble.FakeBleAdvertiser
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.AdvertiserState
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.AdvertisingError
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.StartAdvertisingException
import uk.gov.onelogin.sharing.bluetooth.internal.util.MainDispatcherRule
import uk.gov.onelogin.sharing.bluetooth.peripheral.FakeGattServerManager

class AndroidMdocSessionManagerTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val advertiser = FakeBleAdvertiser()
    private val gattServerManager = FakeGattServerManager()
    private val testScope = CoroutineScope(SupervisorJob() + dispatcherRule.testDispatcher)
    private val sessionManager = AndroidMdocSessionManager(
        bleAdvertiser = advertiser,
        gattServerManager = gattServerManager,
        coroutineScope = testScope
    )
    private val uuid = UUID.randomUUID()

    @Test
    fun `initial state is Idle`() = runTest {
        Assert.assertEquals(MdocSessionState.Idle, sessionManager.state.value)
    }

    @Test
    fun `advertiser state maps to session state`() = runTest {
        sessionManager.state.test {
            Assert.assertEquals(MdocSessionState.Idle, awaitItem())

            advertiser.emitState(AdvertiserState.Started)
            Assert.assertEquals(MdocSessionState.Advertising, awaitItem())

            advertiser.emitState(AdvertiserState.Stopped)
            Assert.assertEquals(MdocSessionState.Stopped, awaitItem())
        }
    }

    @Test
    fun `start triggers advertiser start with correct UUID`() = runTest {
        sessionManager.start(uuid)

        Assert.assertEquals(1, advertiser.startCalls)
        Assert.assertEquals(uuid, advertiser.lastAdvertiseData?.serviceUuid)
        Assert.assertEquals(AdvertiserState.Started, advertiser.state.value)
    }

    @Test
    fun `start sets Error state when advertiser throws`() = runTest {
        val advertiser = FakeBleAdvertiser().apply {
            exceptionToThrow = StartAdvertisingException(AdvertisingError.INTERNAL_ERROR)
        }

        val sessionManager = AndroidMdocSessionManager(
            bleAdvertiser = advertiser,
            gattServerManager = gattServerManager,
            coroutineScope = testScope
        )

        sessionManager.state.test {
            Assert.assertEquals(MdocSessionState.Idle, awaitItem())

            sessionManager.start(uuid)
            Assert.assertEquals(
                MdocSessionState.Error(MdocError.ADVERTISING_FAILED),
                awaitItem()
            )
        }
    }

    @Test
    fun `stop calls advertiser stop and resets session state`() = runTest {
        val advertiser = FakeBleAdvertiser(initialState = AdvertiserState.Started)
        val sessionManager = AndroidMdocSessionManager(
            bleAdvertiser = advertiser,
            gattServerManager = gattServerManager,
            coroutineScope = testScope
        )

        sessionManager.state.test {
            Assert.assertEquals(MdocSessionState.Advertising, awaitItem())

            sessionManager.stop()

            Assert.assertEquals(1, advertiser.stopCalls)
            Assert.assertEquals(MdocSessionState.Stopped, awaitItem())
        }
    }
}
