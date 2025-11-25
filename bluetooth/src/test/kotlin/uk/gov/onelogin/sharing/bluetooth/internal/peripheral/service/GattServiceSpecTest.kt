package uk.gov.onelogin.sharing.bluetooth.internal.peripheral.service

import android.bluetooth.BluetoothGattCharacteristic
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.GattUuids.CLIENT_2_SERVER_UUID
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.GattUuids.SERVER_2_CLIENT_UUID
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.GattUuids.STATE_UUID

@RunWith(Parameterized::class)
class GattServiceSpecTest(
    private val name: String,
    private val expectedUuid: UUID,
    private val expectedProperties: Int,
    private val expectedPermissions: Int,
    private val expectedHasCccd: Boolean
) {
    /*companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf(
                "state characteristic",
                STATE_UUID,
                BluetoothGattCharacteristic.PROPERTY_NOTIFY or
                        BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                BluetoothGattCharacteristic.PERMISSION_WRITE,
                true,
            ),
            arrayOf(
                "client → server characteristic",
                CLIENT_2_SERVER_UUID,
                BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                BluetoothGattCharacteristic.PERMISSION_WRITE,
                false,
            ),
            arrayOf(
                "server → client characteristic",
                SERVER_2_CLIENT_UUID,
                BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_WRITE,
                true,
            ),
        )
    }*/

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf(
                "state characteristic",
                STATE_UUID,
                BluetoothGattCharacteristic.PROPERTY_NOTIFY or
                    BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                BluetoothGattCharacteristic.PERMISSION_WRITE,
                true
            ),
            arrayOf(
                "client → server characteristic",
                CLIENT_2_SERVER_UUID,
                BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                BluetoothGattCharacteristic.PERMISSION_WRITE,
                false
            ),
            arrayOf(
                "server → client characteristic",
                SERVER_2_CLIENT_UUID,
                BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_WRITE,
                true
            )
        )
    }

    @Test
    fun `mdoc service characteristic matches ISO spec`() {
        val spec = GattServiceSpec.mdocService()

        val characteristic = spec.characteristics.firstOrNull { it.uuid == expectedUuid }
        assertNotNull(
            "Characteristic with UUID $expectedUuid not found in spec",
            characteristic
        )

        characteristic!!

        assertEquals(
            "Properties do not match for $name ($expectedUuid)",
            expectedProperties,
            characteristic.properties
        )
        assertEquals(
            "Permissions do not match for $name ($expectedUuid)",
            expectedPermissions,
            characteristic.permissions
        )
        assertEquals(
            "hasCccd flag does not match for $name ($expectedUuid)",
            expectedHasCccd,
            characteristic.hasCccd
        )
    }

    @Test
    fun `mdoc service exposes all expected characteristics`() {
        val spec = GattServiceSpec.mdocService()
        val expectedUuids = setOf(
            STATE_UUID,
            CLIENT_2_SERVER_UUID,
            SERVER_2_CLIENT_UUID
        )

        assertEquals(
            "Unexpected number of characteristics in MDOC GATT service",
            expectedUuids.size,
            spec.characteristics.size
        )

        val actualUuids = spec.characteristics.map { it.uuid }.toSet()
        assertEquals(expectedUuids, actualUuids)
    }
}
