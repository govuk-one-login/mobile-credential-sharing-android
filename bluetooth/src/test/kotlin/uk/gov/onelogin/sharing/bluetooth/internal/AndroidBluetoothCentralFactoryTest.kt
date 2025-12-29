package uk.gov.onelogin.sharing.bluetooth.internal

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.mockk.mockk
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.bluetooth.internal.central.AndroidGattClientManager
import uk.gov.onelogin.sharing.bluetooth.internal.core.AndroidBluetoothStateMonitor

@RunWith(RobolectricTestRunner::class)
class AndroidBluetoothCentralFactoryTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val logger = mockk<Logger>(relaxed = true)

    @Test
    fun `create returns Android central components`() {
        val factory = AndroidBluetoothCentralFactory(
            context = context,
            logger = logger
        )

        val components = factory.create()

        assertNotNull(components.gattClientManager)
        assertNotNull(components.bluetoothStateMonitor)

        assertTrue(components.gattClientManager is AndroidGattClientManager)
        assertTrue(components.bluetoothStateMonitor is AndroidBluetoothStateMonitor)
    }
}
