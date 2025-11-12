package uk.gov.onelogin.sharing.bluetooth.internal.core

import android.bluetooth.le.BluetoothLeAdvertiser

interface BluetoothAdapterProvider {
    fun isEnabled(): Boolean

    fun getAdvertiser(): BluetoothLeAdvertiser?
}
