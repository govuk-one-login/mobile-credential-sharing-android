package uk.gov.onelogin.sharing.bluetooth.ble

import java.util.UUID
import uk.gov.onelogin.sharing.bluetooth.advertiser.BleAdvertiser

data class BleAdvertiseData(val payload: BleAdvertiser.Payload, val serviceUuid: UUID)
