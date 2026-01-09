package uk.gov.onelogin.sharing.bluetooth.internal.central

import kotlin.test.assertEquals
import org.junit.Test

class GattUuidsTest {
    @Test
    fun testUuidValues() {
        assertEquals(
            "00000001-A123-48CE-896B-4C76973373E6",
            GattUuids.STATE_UUID.toString().uppercase()
        )
        assertEquals(
            "00000002-A123-48CE-896B-4C76973373E6",
            GattUuids.CLIENT_2_SERVER_UUID.toString().uppercase()
        )
        assertEquals(
            "00000003-A123-48CE-896B-4C76973373E6",
            GattUuids.SERVER_2_CLIENT_UUID.toString().uppercase()
        )
    }
}
