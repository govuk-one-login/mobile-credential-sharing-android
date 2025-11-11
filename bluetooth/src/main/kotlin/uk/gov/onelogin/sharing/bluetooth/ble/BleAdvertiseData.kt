package uk.gov.onelogin.sharing.bluetooth.ble

import android.bluetooth.le.AdvertiseData
import android.os.ParcelUuid
import java.util.UUID
import uk.gov.onelogin.sharing.bluetooth.advertiser.BleAdvertiser

data class BleAdvertiseData(val payload: BleAdvertiser.Payload, val serviceUuid: UUID)

fun BleAdvertiseData.toAndroid(): AdvertiseData? = AdvertiseData.Builder()
    .addServiceUuid(ParcelUuid(serviceUuid))
    .addServiceData(
        ParcelUuid(serviceUuid),
        payload.asBytes()
    )
    .build()
