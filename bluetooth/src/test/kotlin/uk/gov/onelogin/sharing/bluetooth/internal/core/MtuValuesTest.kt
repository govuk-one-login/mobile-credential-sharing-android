package uk.gov.onelogin.sharing.bluetooth.internal.core

import kotlin.test.assertFailsWith
import org.junit.Assert.assertEquals
import org.junit.Test

class MtuValuesTest {

    @Test
    fun `maxChunkBytes returns mtu minus headers when within range and below cap`() {
        assertEquals(20, MtuValues.maxChunkBytes(MtuValues.MIN_MTU))

        assertEquals(21, MtuValues.maxChunkBytes(24))

        assertEquals(97, MtuValues.maxChunkBytes(100))
    }

    @Test
    fun `maxChunkBytes caps payload at MAX_LENGTH`() {
        assertEquals(
            MtuValues.MAX_LENGTH,
            MtuValues.maxChunkBytes(515)
        )

        assertEquals(
            MtuValues.MAX_LENGTH,
            MtuValues.maxChunkBytes(MtuValues.MAX_MTU)
        )
    }

    @Test
    fun `maxChunkBytes throws exception when mtu is below MIN_MTU`() {
        assertFailsWith<IllegalArgumentException> {
            MtuValues.maxChunkBytes(MtuValues.MIN_MTU - 1)
        }
    }

    @Test
    fun `maxChunkBytes throws exception when mtu is above MAX_MTU`() {
        assertFailsWith<IllegalArgumentException> {
            MtuValues.maxChunkBytes(MtuValues.MAX_MTU + 1)
        }
    }
}
