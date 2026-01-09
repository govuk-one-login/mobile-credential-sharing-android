package uk.gov.onelogin.sharing.bluetooth.internal.central

import kotlin.test.assertEquals
import org.junit.Test
import uk.gov.onelogin.sharing.bluetooth.internal.central.MtuValues

class MtuValuesTest {
    @Test
    fun `mtu value is correct`() {
        assertEquals(515, MtuValues.MAX_POSSIBLE)
    }
}
