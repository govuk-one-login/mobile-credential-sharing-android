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
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.AndroidBleAdvertiser
import uk.gov.onelogin.sharing.bluetooth.internal.central.FakeGattWriter
import uk.gov.onelogin.sharing.bluetooth.internal.core.AndroidBluetoothStateMonitor
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.AndroidGattServerManager
import uk.gov.onelogin.sharing.bluetooth.permissions.StubBluetoothPermissionChecker

@RunWith(RobolectricTestRunner::class)
class AndroidBluetoothPeripheralFactoryTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val logger = mockk<Logger>(relaxed = true)

    @Test
    fun `create returns Android peripheral components`() {
        val factory = AndroidBluetoothPeripheralFactory(
            bluetoothPermissionChecker = StubBluetoothPermissionChecker(),
            context = context,
            logger = logger,
            gattWriter = FakeGattWriter()
        )

        val components = factory.create()

        assertNotNull(components.advertiser)
        assertNotNull(components.gattServerManager)
        assertNotNull(components.bluetoothStateMonitor)

        assertTrue(components.advertiser is AndroidBleAdvertiser)
        assertTrue(components.gattServerManager is AndroidGattServerManager)
        assertTrue(components.bluetoothStateMonitor is AndroidBluetoothStateMonitor)
    }
}
