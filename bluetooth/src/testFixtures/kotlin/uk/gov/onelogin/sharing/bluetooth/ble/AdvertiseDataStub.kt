package uk.gov.onelogin.sharing.bluetooth.ble

import java.util.UUID
import uk.gov.onelogin.sharing.bluetooth.api.advertising.BleAdvertiseData

fun stubBleAdvertiseData(serviceUuid: UUID = UUID.randomUUID()) = BleAdvertiseData(
    serviceUuid = serviceUuid
)
