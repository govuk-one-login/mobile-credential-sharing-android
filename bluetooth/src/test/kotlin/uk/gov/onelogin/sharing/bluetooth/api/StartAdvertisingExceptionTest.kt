package uk.gov.onelogin.sharing.bluetooth.api

import org.junit.Assert.assertEquals
import org.junit.Test

class StartAdvertisingExceptionTest {

    @Test
    fun `toString returns correct message for each error`() {
        assertEquals(
            "Bluetooth is disabled",
            AdvertisingError.BLUETOOTH_DISABLED.toString()
        )
        assertEquals(
            "Missing bluetooth permission",
            AdvertisingError.MISSING_PERMISSION.toString()
        )
        assertEquals(
            "Advertising already in progress",
            AdvertisingError.ALREADY_IN_PROGRESS.toString()
        )
        assertEquals(
            "Invalid UUID",
            AdvertisingError.INVALID_UUID.toString()
        )
        assertEquals(
            "Start advertising timed out",
            AdvertisingError.START_TIMEOUT.toString()
        )
        assertEquals(
            "Start advertising cancelled",
            AdvertisingError.CANCELLED.toString()
        )
        assertEquals(
            "Internal error",
            AdvertisingError.INTERNAL_ERROR.toString()
        )
    }

    @Test
    fun `exposes error and formats message`() {
        val exception = StartAdvertisingException(AdvertisingError.INVALID_UUID)

        assertEquals(AdvertisingError.INVALID_UUID, exception.error)
        assertEquals("Error: Invalid UUID", exception.message)
    }
}
