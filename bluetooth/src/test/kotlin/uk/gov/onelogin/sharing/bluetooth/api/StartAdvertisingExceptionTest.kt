package uk.gov.onelogin.sharing.bluetooth.api

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class StartAdvertisingExceptionTest(
    private val error: AdvertisingError,
    private val expectedMessage: String
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf(AdvertisingError.BLUETOOTH_DISABLED, "Error: Bluetooth is disabled"),
            arrayOf(AdvertisingError.MISSING_PERMISSION, "Error: Missing bluetooth permission"),
            arrayOf(AdvertisingError.ALREADY_IN_PROGRESS, "Error: Advertising already in progress"),
            arrayOf(AdvertisingError.INVALID_UUID, "Error: Invalid UUID"),
            arrayOf(AdvertisingError.START_TIMEOUT, "Error: Start advertising timed out"),
            arrayOf(AdvertisingError.INTERNAL_ERROR, "Error: Internal error")
        )
    }

    @Test
    fun `exposes error and formats message`() {
        val exception = StartAdvertisingException(error)

        assertEquals(error, exception.error)
        assertEquals(expectedMessage, exception.message)
    }
}
