package uk.gov.onelogin.sharing.bluetooth.api

import android.bluetooth.le.AdvertiseData
import android.os.ParcelUuid
import java.util.UUID

data class BleAdvertiseData(val payload: BleAdvertiser.Payload, val serviceUuid: UUID)

fun BleAdvertiseData.toAndroid(): AdvertiseData? = AdvertiseData.Builder()
    .addServiceUuid(ParcelUuid(serviceUuid))
    .addServiceData(
        ParcelUuid(serviceUuid),
        payload.asBytes()
    )
    .build()
