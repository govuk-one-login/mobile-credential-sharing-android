package uk.gov.onelogin.sharing.bluetooth.ble

import android.bluetooth.le.AdvertisingSetCallback
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdvertisingFailureReasonTest {

    @Test
    fun `toString returns stable, user friendly messages`() {
        assertEquals(
            "Advertiser is null",
            AdvertisingFailureReason.ADVERTISER_NULL.toString()
        )
        assertEquals(
            "Advertising failed due to security exception",
            AdvertisingFailureReason.ADVERTISE_FAILED_SECURITY_EXCEPTION.toString()
        )
        assertEquals(
            "Advertising already started",
            AdvertisingFailureReason.ALREADY_STARTED.toString()
        )
        assertEquals(
            "Advertising failed due to data too large",
            AdvertisingFailureReason.ADVERTISE_FAILED_DATA_TOO_LARGE.toString()
        )
        assertEquals(
            "Advertising failed due to internal error",
            AdvertisingFailureReason.ADVERTISE_FAILED_INTERNAL_ERROR.toString()
        )
        assertEquals(
            "Advertising failed due to too many advertisers",
            AdvertisingFailureReason.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS.toString()
        )
        assertEquals(
            "Advertising failed due to feature unsupported",
            AdvertisingFailureReason.ADVERTISE_FAILED_FEATURE_UNSUPPORTED.toString()
        )
        assertEquals(
            "Unknown reason",
            AdvertisingFailureReason.UNKNOWN.toString()
        )
    }

    @Test
    fun `to reason maps all known android statuses`() {
        assertEquals(
            AdvertisingFailureReason.ALREADY_STARTED,
            AdvertisingSetCallback.ADVERTISE_FAILED_ALREADY_STARTED.toReason()
        )
        assertEquals(
            AdvertisingFailureReason.ADVERTISE_FAILED_DATA_TOO_LARGE,
            AdvertisingSetCallback.ADVERTISE_FAILED_DATA_TOO_LARGE.toReason()
        )
        assertEquals(
            AdvertisingFailureReason.ADVERTISE_FAILED_FEATURE_UNSUPPORTED,
            AdvertisingSetCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED.toReason()
        )
        assertEquals(
            AdvertisingFailureReason.ADVERTISE_FAILED_INTERNAL_ERROR,
            AdvertisingSetCallback.ADVERTISE_FAILED_INTERNAL_ERROR.toReason()
        )
        assertEquals(
            AdvertisingFailureReason.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS,
            AdvertisingSetCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS.toReason()
        )
    }

    @Test
    fun `Unknown int reason code maps to unknown`() {
        assertEquals(AdvertisingFailureReason.UNKNOWN, 9999.toReason())
    }

    @Test
    fun `every reason has a non-empty to string`() {
        AdvertisingFailureReason.entries.forEach {
            assertTrue(
                "Empty message for $it",
                it.toString().isNotBlank()
            )
        }
    }
}
