package uk.gov.onelogin.sharing.bluetooth.ble

import uk.gov.onelogin.sharing.bluetooth.advertiser.BleAdvertiser
import java.util.UUID

fun stubBleAdvertiseData(
    payload: BleAdvertiser.Payload = object : BleAdvertiser.Payload {
        override fun asBytes(): ByteArray = "FakePayload".toByteArray()
    },
    serviceUuid: UUID = UUID.randomUUID()
) = BleAdvertiseData(
    payload = payload,
    serviceUuid = serviceUuid
)
