package uk.gov.onelogin.sharing.bluetooth.internal.central

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothStatusCodes
import android.os.Build
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class AndroidGattWriterTest {
    private val writer = AndroidGattWriter()

    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    @Test
    fun `writeCharacteristic and returns true on SUCCESS - modern API`() {
        val gatt = mockk<BluetoothGatt>()
        val characteristic = mockk<BluetoothGattCharacteristic>(relaxed = true)
        val value = byteArrayOf(0x01)

        every {
            gatt.writeCharacteristic(
                characteristic,
                value,
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            )
        } returns BluetoothStatusCodes.SUCCESS

        val result = writer.writeCharacteristic(gatt, characteristic, value)

        assertTrue(result)
    }

    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    @Test
    fun `writeCharacteristic returns false on non-SUCCESS - modern API`() {
        val gatt = mockk<BluetoothGatt>()
        val characteristic = mockk<BluetoothGattCharacteristic>(relaxed = true)
        val value = byteArrayOf(0x01)

        every {
            gatt.writeCharacteristic(
                characteristic,
                value,
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            )
        } returns BluetoothStatusCodes.ERROR_UNKNOWN

        val result = writer.writeCharacteristic(gatt, characteristic, value)

        assertFalse(result)
    }

    @Suppress("DEPRECATION")
    @Config(sdk = [Build.VERSION_CODES.S])
    @Test
    fun `writeCharacteristic returns true on successful write - deprecated API`() {
        val gatt = mockk<BluetoothGatt>()
        val characteristic = mockk<BluetoothGattCharacteristic>(relaxed = true)
        val value = byteArrayOf(0x01)

        every { gatt.writeCharacteristic(characteristic) } returns true

        val result = writer.writeCharacteristic(gatt, characteristic, value)

        assertTrue(result)
    }

    @Suppress("DEPRECATION")
    @Config(sdk = [Build.VERSION_CODES.S])
    @Test
    fun `writeCharacteristic returns false on failed write - deprecated API`() {
        val gatt = mockk<BluetoothGatt>()
        val characteristic = mockk<BluetoothGattCharacteristic>(relaxed = true)
        val value = byteArrayOf(0x01)

        every { gatt.writeCharacteristic(characteristic) } returns false

        val result = writer.writeCharacteristic(gatt, characteristic, value)

        assertFalse(result)
    }
}
