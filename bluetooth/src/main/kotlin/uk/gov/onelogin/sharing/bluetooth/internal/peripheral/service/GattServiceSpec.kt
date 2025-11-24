package uk.gov.onelogin.sharing.bluetooth.internal.peripheral.service

import android.bluetooth.BluetoothGattCharacteristic
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.GattUuids.CLIENT_2_SERVER_UUID
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.GattUuids.MDOC_SERVICE_ID
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.GattUuids.SERVER_2_CLIENT_UUID
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.GattUuids.STATE_UUID

internal object GattServiceSpec {
    fun mdocService(): GattServiceDefinition =
        GattServiceDefinition(
            uuid = MDOC_SERVICE_ID,
            characteristics = listOf(
                // state characteristic (properties: notify, write without response)
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