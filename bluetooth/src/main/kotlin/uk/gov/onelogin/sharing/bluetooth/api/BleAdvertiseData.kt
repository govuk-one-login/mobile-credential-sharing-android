package uk.gov.onelogin.sharing.bluetooth.api

import java.util.UUID

/**
 * Represents the data to be advertised.
 *
 * @param serviceUuid The [UUID] of the service to be advertised.
 */
data class BleAdvertiseData(val serviceUuid: UUID)
