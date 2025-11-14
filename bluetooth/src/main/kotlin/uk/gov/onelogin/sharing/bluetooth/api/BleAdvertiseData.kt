package uk.gov.onelogin.sharing.bluetooth.api

import android.bluetooth.le.AdvertiseData
import android.os.ParcelUuid
import java.util.UUID

/**
 * Represents the data to be advertised.
 *
 * It currently only contains the service UUID but additional data can be added in the future.
 * The toAndroid() converts this to the Android-specific advertising data.
 * §
 * @param serviceUuid The [UUID] of the service to be advertised.
 */
data class BleAdvertiseData(val serviceUuid: UUID)

fun BleAdvertiseData.toAndroid(): AdvertiseData? {
    println(serviceUuid)
    return AdvertiseData.Builder()
        .addServiceUuid(ParcelUuid(serviceUuid))
        .build()
}
