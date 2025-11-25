package uk.gov.onelogin.sharing.bluetooth.internal.advertising

import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Test

class BleUuidValidatorTest {

    @Test
    fun `validate accepts normal random uuid`() {
        val uuid = UUID.randomUUID()

        BleUuidValidator.isValid(uuid)
    }

    @Test
    fun `validate rejects all-zero uuid`() {
        val zero = UUID(0L, 0L)

        assertEquals(false, BleUuidValidator.isValid(zero))
    }
}
