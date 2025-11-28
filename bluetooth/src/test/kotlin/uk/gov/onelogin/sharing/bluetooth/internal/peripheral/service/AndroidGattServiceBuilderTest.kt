package uk.gov.onelogin.sharing.bluetooth.internal.peripheral.service

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.GattUuids

@RunWith(RobolectricTestRunner::class)
class AndroidGattServiceBuilderTest {
    private val serviceUuid = UUID.randomUUID()

    @Test
    fun `builds Android GATT service matching spec`() {
        val spec = GattServiceDefinition(
            uuid = serviceUuid,
            characteristics = listOf(
                GattCharacteristicDefinition(
                    uuid = GattUuids.STATE_UUID,
                    properties = BluetoothGattCharacteristic.PROPERTY_NOTIFY or
                        BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                    permissions = BluetoothGattCharacteristic.PERMISSION_WRITE,
                    hasCccd = true
                ),
                GattCharacteristicDefinition(
                    uuid = GattUuids.CLIENT_2_SERVER_UUID,
                    properties = BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                    permissions = BluetoothGattCharacteristic.PERMISSION_WRITE,
                    hasCccd = false
                ),
                GattCharacteristicDefinition(
                    uuid = GattUuids.SERVER_2_CLIENT_UUID,
                    properties = BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                    permissions = BluetoothGattCharacteristic.PERMISSION_WRITE,
                    hasCccd = true
                )
            )
        )

        val service: BluetoothGattService = AndroidGattServiceBuilder.build(spec)

        assertEquals(serviceUuid, service.uuid)
        assertEquals(BluetoothGattService.SERVICE_TYPE_PRIMARY, service.type)

        val characteristics = service.characteristics
        assertEquals(3, characteristics.size)

        // STATE characteristic
        val state = characteristics.first { it.uuid == GattUuids.STATE_UUID }
        assertEquals(
            BluetoothGattCharacteristic.PROPERTY_NOTIFY or
                BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
            state.properties
        )
        assertEquals(BluetoothGattCharacteristic.PERMISSION_WRITE, state.permissions)

        val stateCccd = state.descriptors.firstOrNull()
        assertNotNull(stateCccd)
        assertEquals(GattUuids.CLIENT_CHARACTERISTIC_CONFIG_UUID, stateCccd!!.uuid)
        assertEquals(
            BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE,
            stateCccd.permissions
        )

        // CLIENT -> SERVER characteristic
        val c2s = characteristics.first { it.uuid == GattUuids.CLIENT_2_SERVER_UUID }
        assertEquals(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE, c2s.properties)
        assertEquals(BluetoothGattCharacteristic.PERMISSION_WRITE, c2s.permissions)
        assertTrue(c2s.descriptors.isEmpty())

        // SERVER -> CLIENT characteristic
        val s2c = characteristics.first { it.uuid == GattUuids.SERVER_2_CLIENT_UUID }
        assertEquals(BluetoothGattCharacteristic.PROPERTY_NOTIFY, s2c.properties)
        assertEquals(BluetoothGattCharacteristic.PERMISSION_WRITE, s2c.permissions)

        val s2cCccd = s2c.descriptors.firstOrNull()
        assertNotNull(s2cCccd)
        assertEquals(GattUuids.CLIENT_CHARACTERISTIC_CONFIG_UUID, s2cCccd!!.uuid)
    }
}
