package uk.gov.onelogin.sharing.bluetooth.ble

import java.util.UUID
import uk.gov.onelogin.sharing.bluetooth.advertiser.BleAdvertiser

fun stubBleAdvertiseData(
    payload: BleAdvertiser.Payload = object : BleAdvertiser.Payload {
        override fun asBytes(): ByteArray = "FakePayload".toByteArray()
    },
    serviceUuid: UUID = UUID.randomUUID()
) = BleAdvertiseData(
    payload = payload,
    serviceUuid = serviceUuid
)
