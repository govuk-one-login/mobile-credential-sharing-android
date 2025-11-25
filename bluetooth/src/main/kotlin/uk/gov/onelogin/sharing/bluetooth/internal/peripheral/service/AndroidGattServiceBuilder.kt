package uk.gov.onelogin.sharing.bluetooth.internal.peripheral.service

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.GattUuids.CLIENT_CHARACTERISTIC_CONFIG_UUID

internal object AndroidGattServiceBuilder {
    fun build(spec: GattServiceDefinition): BluetoothGattService {
        val service = BluetoothGattService(
            spec.uuid,
            BluetoothGattService.SERVICE_TYPE_PRIMARY
        )

        spec.characteristics.forEach {
            val characteristic = BluetoothGattCharacteristic(
                it.uuid,
                it.properties,
                it.permissions
            )
            if (it.hasCccd) {
                characteristic.addDescriptor(
                    BluetoothGattDescriptor(
                        CLIENT_CHARACTERISTIC_CONFIG_UUID,
                        BluetoothGattDescriptor.PERMISSION_READ
                            or BluetoothGattDescriptor.PERMISSION_WRITE
                    )
                )
            }
            service.addCharacteristic(characteristic)
        }

        return service
    }
}
