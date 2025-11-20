package uk.gov.onelogin.sharing.bluetooth.api

import java.util.UUID

/**
 * Validates UUIDs used in BLE advertising.
 */
object BleUuidValidator {

    /**
     * Returns false if the UUID is not valid valid.
     *
     * Additional format validation should be added here.
     */
    fun isValid(uuid: UUID): Boolean =
        !(uuid.mostSignificantBits == 0L && uuid.leastSignificantBits == 0L)
}
