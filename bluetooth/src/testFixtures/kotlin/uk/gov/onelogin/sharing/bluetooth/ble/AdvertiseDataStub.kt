package uk.gov.onelogin.sharing.bluetooth.ble

import java.util.UUID
import uk.gov.onelogin.sharing.bluetooth.api.BleAdvertiseData

fun stubBleAdvertiseData(serviceUuid: UUID = UUID.randomUUID()) = BleAdvertiseData(
    serviceUuid = serviceUuid
)
