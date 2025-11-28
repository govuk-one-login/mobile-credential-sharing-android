package uk.gov.onelogin.sharing.bluetooth.internal.peripheral.service

import android.bluetooth.BluetoothGattCharacteristic
import java.util.UUID
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.GattUuids.CLIENT_2_SERVER_UUID
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.GattUuids.SERVER_2_CLIENT_UUID
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.GattUuids.STATE_UUID

/**
 * Mdoc service characteristics for the BLE GATT service as specified in
 * ISO/IEC 18013-5:2021(E), Section 11.1.3.2 Service definition - Table 5
 *
 */
internal object GattServiceSpec {
    fun mdocService(serviceUuid: UUID): GattServiceDefinition = GattServiceDefinition(
        uuid = serviceUuid,
        characteristics = listOf(
            // state (properties: notify, write without response)
            GattCharacteristicDefinition(
                uuid = STATE_UUID,
                properties =
                    BluetoothGattCharacteristic.PROPERTY_NOTIFY or
                        BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                permissions = BluetoothGattCharacteristic.PERMISSION_WRITE,
                hasCccd = true
            ),
            // Client -> Server (properties: write without response)
            GattCharacteristicDefinition(
                uuid = CLIENT_2_SERVER_UUID,
                properties = BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                permissions = BluetoothGattCharacteristic.PERMISSION_WRITE
            ),
            // Server -> Client (properties: notify)
            GattCharacteristicDefinition(
                uuid = SERVER_2_CLIENT_UUID,
                properties = BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                permissions = BluetoothGattCharacteristic.PERMISSION_WRITE,
                hasCccd = true
            )
        )
    )
}
