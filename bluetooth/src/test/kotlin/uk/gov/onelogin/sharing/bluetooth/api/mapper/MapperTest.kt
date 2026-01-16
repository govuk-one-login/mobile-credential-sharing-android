package uk.gov.onelogin.sharing.bluetooth.api.mapper

import android.bluetooth.le.AdvertisingSetParameters.INTERVAL_MEDIUM
import android.bluetooth.le.AdvertisingSetParameters.TX_POWER_LOW
import kotlin.test.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import uk.gov.onelogin.sharing.bluetooth.api.advertising.AdvertisingParameters
import uk.gov.onelogin.sharing.bluetooth.internal.mapper.AdvertisingParametersMapper

@RunWith(RobolectricTestRunner::class)
class MapperTest {
    @Test
    fun `advertising set parameters are set correctly by the mapper`() {
        val parameters = AdvertisingParameters(
            legacyMode = false,
            interval = INTERVAL_MEDIUM,
            txPowerLevel = TX_POWER_LOW,
            connectable = false,
            scannable = false
        )

        val setParameters = AdvertisingParametersMapper
            .toAndroid(parameters)

        assertEquals(parameters.legacyMode, setParameters.isLegacy)
        assertEquals(parameters.interval, setParameters.interval)
        assertEquals(parameters.txPowerLevel, setParameters.txPowerLevel)
        assertEquals(parameters.connectable, setParameters.isConnectable)
        assertEquals(parameters.scannable, setParameters.isScannable)
    }
}
