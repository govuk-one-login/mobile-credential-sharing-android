package uk.gov.onelogin.sharing.bluetooth.internal.core

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import uk.gov.onelogin.sharing.bluetooth.ble.BluetoothContext

@RunWith(RobolectricTestRunner::class)
class AndroidBluetoothStateMonitorTest {
    private val app = ApplicationProvider.getApplicationContext<Application>()
    private lateinit var monitor: AndroidBluetoothStateMonitor
    private val mockAdapter: BluetoothAdapter = mockk()
    private val mockManager: BluetoothManager = mockk()
    private val context = BluetoothContext(app, mockManager)

    @Before
    fun setup() {
        every { mockManager.adapter } returns mockAdapter
        monitor = AndroidBluetoothStateMonitor(context)
    }

    @After
    fun tearDown() {
        monitor.stop()
    }

    @Test
    fun `start emits initial ON state when bluetooth is enabled`() = runTest {
        every { mockAdapter.isEnabled } returns true

        monitor.states.test {
            monitor.start()

            assertEquals(BluetoothStatus.ON, awaitItem())
        }
    }

    @Test
    fun `start emits initial OFF state when bluetooth is disabled`() = runTest {
        every { mockAdapter.isEnabled } returns false

        monitor.states.test {
            monitor.start()

            assertEquals(BluetoothStatus.OFF, awaitItem())
        }
    }

    @Test
    fun `broadcast receiver emits TURNING_OFF and OFF`() = runTest {
        every { mockAdapter.isEnabled } returns true

        val intentTurningOff = Intent(BluetoothAdapter.ACTION_STATE_CHANGED).apply {
            putExtra(
                BluetoothAdapter.EXTRA_STATE,
                BluetoothAdapter.STATE_TURNING_OFF
            )
        }

        val intentOff = Intent(BluetoothAdapter.ACTION_STATE_CHANGED).apply {
            putExtra(
                BluetoothAdapter.EXTRA_STATE,
                BluetoothAdapter.STATE_OFF
            )
        }

        monitor.states.test {
            monitor.start()

            assertEquals(BluetoothStatus.ON, awaitItem())

            context.sendBroadcast(intentTurningOff)
            shadowOf(Looper.getMainLooper()).idle()
            assertEquals(BluetoothStatus.TURNING_OFF, awaitItem())

            context.sendBroadcast(intentOff)
            shadowOf(Looper.getMainLooper()).idle()
            assertEquals(BluetoothStatus.OFF, awaitItem())
        }
    }
}
