package uk.gov.onelogin.sharing.bluetooth.internal.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Enclosed::class)
class MtuValuesTest {

    @RunWith(Parameterized::class)
    class NormalRangeTests(private val mtu: Int, private val expected: Int) {
        @Test
        fun `maxChunkBytes returns mtu minus headers when within range and below cap`() {
            assertEquals(expected, MtuValues.maxChunkBytes(mtu))
        }

        companion object {
            @JvmStatic
            @Parameters(name = "mtu={0}, expected={1}")
            fun data(): Collection<Array<Any>> = listOf(
                arrayOf(MtuValues.MIN_MTU, 20),
                arrayOf(24, 21),
                arrayOf(100, 97)
            )
        }
    }

    @RunWith(Parameterized::class)
    class CappedRangeTests(private val mtu: Int) {
        @Test
        fun `maxChunkBytes caps payload at MAX_LENGTH`() {
            assertEquals(MtuValues.MAX_LENGTH, MtuValues.maxChunkBytes(mtu))
        }

        companion object {
            @JvmStatic
            @Parameters(name = "mtu={0}")
            fun data(): Collection<Array<Any>> = listOf(
                arrayOf(515),
                arrayOf(MtuValues.MAX_MTU)
            )
        }
    }

    @RunWith(Parameterized::class)
    class InvalidMtuTests(private val mtu: Int) {
        @Test
        fun `maxChunkBytes throws IllegalArgumentException for invalid mtu`() {
            assertThrows(IllegalArgumentException::class.java) {
                MtuValues.maxChunkBytes(mtu)
            }
        }

        companion object {
            @JvmStatic
            @Parameters(name = "invalid mtu={0}")
            fun data(): Collection<Array<Any>> = listOf(
                arrayOf(MtuValues.MIN_MTU - 1),
                arrayOf(MtuValues.MAX_MTU + 1)
            )
        }
    }
}
