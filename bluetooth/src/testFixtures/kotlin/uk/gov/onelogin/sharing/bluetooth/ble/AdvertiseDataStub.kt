package uk.gov.onelogin.sharing.bluetooth.ble

import java.util.UUID
import uk.gov.onelogin.sharing.bluetooth.api.BleAdvertiseData
import uk.gov.onelogin.sharing.bluetooth.api.BleAdvertiser

fun stubBleAdvertiseData(
    payload: BleAdvertiser.Payload = BleAdvertiser.Payload { "FakePayload".toByteArray() },
    serviceUuid: UUID = UUID.randomUUID()
) = BleAdvertiseData(
    payload = payload,
    serviceUuid = serviceUuid
)
