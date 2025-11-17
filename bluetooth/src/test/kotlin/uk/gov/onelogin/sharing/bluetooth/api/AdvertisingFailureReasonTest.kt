package uk.gov.onelogin.sharing.bluetooth.api

import android.bluetooth.le.AdvertisingSetCallback
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class AdvertisingFailureReasonToStringTest(
    private val reason: AdvertisingFailureReason,
    private val expected: String
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0} -> \"{1}\"")
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf(AdvertisingFailureReason.ADVERTISER_NULL, "Advertiser is null"),
            arrayOf(
                AdvertisingFailureReason.ADVERTISE_FAILED_SECURITY_EXCEPTION,
                "Advertising failed due to security exception"
            ),
            arrayOf(
                AdvertisingFailureReason.ALREADY_STARTED,
                "Advertising already started"
            ),
            arrayOf(
                AdvertisingFailureReason.ADVERTISE_FAILED_DATA_TOO_LARGE,
                "Advertising failed due to data too large"
            ),
            arrayOf(
                AdvertisingFailureReason.ADVERTISE_FAILED_INTERNAL_ERROR,
                "Advertising failed due to internal error"
            ),
            arrayOf(
                AdvertisingFailureReason.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS,
                "Advertising failed due to too many advertisers"
            ),
            arrayOf(
                AdvertisingFailureReason.ADVERTISE_FAILED_FEATURE_UNSUPPORTED,
                "Advertising failed due to feature unsupported"
            ),
            arrayOf(
                AdvertisingFailureReason.UNKNOWN,
                "Unknown reason"
            )
        )
    }

    @Test
    fun `toString returns stable, user friendly messages`() {
        Assert.assertEquals(expected, reason.toString())
    }
}

@RunWith(Parameterized::class)
class AdvertisingFailureReasonMappingTest(
    private val androidCode: Int,
    private val expectedReason: AdvertisingFailureReason
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: code={0} -> {1}")
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf(
                AdvertisingSetCallback.ADVERTISE_FAILED_ALREADY_STARTED,
                AdvertisingFailureReason.ALREADY_STARTED
            ),
            arrayOf(
                AdvertisingSetCallback.ADVERTISE_FAILED_DATA_TOO_LARGE,
                AdvertisingFailureReason.ADVERTISE_FAILED_DATA_TOO_LARGE
            ),
            arrayOf(
                AdvertisingSetCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED,
                AdvertisingFailureReason.ADVERTISE_FAILED_FEATURE_UNSUPPORTED
            ),
            arrayOf(
                AdvertisingSetCallback.ADVERTISE_FAILED_INTERNAL_ERROR,
                AdvertisingFailureReason.ADVERTISE_FAILED_INTERNAL_ERROR
            ),
            arrayOf(
                AdvertisingSetCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS,
                AdvertisingFailureReason.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS
            ),
            arrayOf(
                9999,
                AdvertisingFailureReason.UNKNOWN
            )
        )
    }

    @Test
    fun `to reason maps all known android statuses`() {
        Assert.assertEquals(expectedReason, androidCode.toReason())
    }
}

class AdvertisingFailureReasonGenericTest {
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
