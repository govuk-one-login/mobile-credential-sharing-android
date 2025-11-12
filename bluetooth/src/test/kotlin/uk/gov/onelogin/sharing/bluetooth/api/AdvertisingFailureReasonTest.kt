package uk.gov.onelogin.sharing.bluetooth.api

import android.bluetooth.le.AdvertisingSetCallback
import org.junit.Assert
import org.junit.Test

class AdvertisingFailureReasonTest {

    @Test
    fun `toString returns stable, user friendly messages`() {
        Assert.assertEquals(
            "Advertiser is null",
            AdvertisingFailureReason.ADVERTISER_NULL.toString()
        )
        Assert.assertEquals(
            "Advertising failed due to security exception",
            AdvertisingFailureReason.ADVERTISE_FAILED_SECURITY_EXCEPTION.toString()
        )
        Assert.assertEquals(
            "Advertising already started",
            AdvertisingFailureReason.ALREADY_STARTED.toString()
        )
        Assert.assertEquals(
            "Advertising failed due to data too large",
            AdvertisingFailureReason.ADVERTISE_FAILED_DATA_TOO_LARGE.toString()
        )
        Assert.assertEquals(
            "Advertising failed due to internal error",
            AdvertisingFailureReason.ADVERTISE_FAILED_INTERNAL_ERROR.toString()
        )
        Assert.assertEquals(
            "Advertising failed due to too many advertisers",
            AdvertisingFailureReason.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS.toString()
        )
        Assert.assertEquals(
            "Advertising failed due to feature unsupported",
            AdvertisingFailureReason.ADVERTISE_FAILED_FEATURE_UNSUPPORTED.toString()
        )
        Assert.assertEquals(
            "Unknown reason",
            AdvertisingFailureReason.UNKNOWN.toString()
        )
    }

    @Test
    fun `to reason maps all known android statuses`() {
        Assert.assertEquals(
            AdvertisingFailureReason.ALREADY_STARTED,
            AdvertisingSetCallback.ADVERTISE_FAILED_ALREADY_STARTED.toReason()
        )
        Assert.assertEquals(
            AdvertisingFailureReason.ADVERTISE_FAILED_DATA_TOO_LARGE,
            AdvertisingSetCallback.ADVERTISE_FAILED_DATA_TOO_LARGE.toReason()
        )
        Assert.assertEquals(
            AdvertisingFailureReason.ADVERTISE_FAILED_FEATURE_UNSUPPORTED,
            AdvertisingSetCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED.toReason()
        )
        Assert.assertEquals(
            AdvertisingFailureReason.ADVERTISE_FAILED_INTERNAL_ERROR,
            AdvertisingSetCallback.ADVERTISE_FAILED_INTERNAL_ERROR.toReason()
        )
        Assert.assertEquals(
            AdvertisingFailureReason.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS,
            AdvertisingSetCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS.toReason()
        )
    }

    @Test
    fun `Unknown int reason code maps to unknown`() {
        Assert.assertEquals(AdvertisingFailureReason.UNKNOWN, 9999.toReason())
    }

    @Test
    fun `every reason has a non-empty to string`() {
        AdvertisingFailureReason.entries.forEach {
            Assert.assertTrue(
                "Empty message for $it",
                it.toString().isNotBlank()
            )
        }
    }
}
