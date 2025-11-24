package uk.gov.onelogin.sharing.bluetooth.internal.core

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import androidx.annotation.RequiresPermission
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.GattUuids.CLIENT_2_SERVER_UUID
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.GattUuids.CLIENT_CHARACTERISTIC_CONFIG_UUID
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.GattUuids.MDOC_SERVICE_ID
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.GattUuids.SERVER_2_CLIENT_UUID
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.GattUuids.STATE_UUID
import java.util.UUID

internal class AndroidBluetoothAdapterProvider(val context: Context) : BluetoothAdapterProvider {
    private val bluetoothManager by lazy {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }

    override fun isEnabled(): Boolean = bluetoothManager.adapter.isEnabled

    override fun getAdvertiser(): BluetoothLeAdvertiser? =
        bluetoothManager.adapter.bluetoothLeAdvertiser

    val bluetoothGattServerCallback: BluetoothGattServerCallback =
        object : BluetoothGattServerCallback() {
            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            override fun onConnectionStateChange(
                device: BluetoothDevice?,
                status: Int,
                newState: Int
            ) {
                println("address: ${device?.address}")
                println("status: $status")
                println("newState: $newState")
                device?.uuids?.forEach {
                    println("uuid: ${it.uuid}")
                }
            }

            override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
                println("status: $status")
            }
        }

}
