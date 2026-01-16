package uk.gov.onelogin.sharing.bluetooth.api.advertising

import android.bluetooth.le.AdvertisingSetParameters.INTERVAL_HIGH
import android.bluetooth.le.AdvertisingSetParameters.TX_POWER_MEDIUM
import kotlin.test.assertEquals
import org.junit.Test

class AdvertisingParametersTest {
    @Test
    fun `advertising parameters are set correctly by default`() {
        val parameters = AdvertisingParameters()
        assertEquals(true, parameters.legacyMode)
        assertEquals(INTERVAL_HIGH, parameters.interval)
        assertEquals(TX_POWER_MEDIUM, parameters.txPowerLevel)
        assertEquals(true, parameters.legacyMode)
        assertEquals(true, parameters.scannable)
    }
}
