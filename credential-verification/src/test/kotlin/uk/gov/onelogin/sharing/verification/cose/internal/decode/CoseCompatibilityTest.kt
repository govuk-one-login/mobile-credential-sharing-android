package uk.gov.onelogin.sharing.verification.cose.internal.decode

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class CoseCompatibilityTest {

    private val decoder = CoseSign1Decoder()

    @Test
    fun `verifies attached_mso vector results in Attached mode`() {
        val hex = loadVector("attached_mso.hex")
        val result = decoder.decode(hex)

        assertThat(result.payloadMode, equalTo(InternalCoseSign1.PayloadMode.ATTACHED))
        assertThat(result.payload?.size, equalTo(3))
    }

    @Test
    fun `verifies detached_reader vector results in Detached mode`() {
        val hex = loadVector("detached_reader.hex")
        val result = decoder.decode(hex)

        assertThat(result.payloadMode, equalTo(InternalCoseSign1.PayloadMode.DETACHED))
        assertThat(result.payload, equalTo(null))
        assertThat(result.signature.size, equalTo(4))
    }

    @Test
    fun `verifies detached_device vector results in Detached mode`() {
        val hex = loadVector("detached_device.hex")
        val result = decoder.decode(hex)

        assertThat(result.payloadMode, equalTo(InternalCoseSign1.PayloadMode.DETACHED))
        assertThat(result.payload, equalTo(null))
    }

    private fun loadVector(filename: String): ByteArray {
        val hexString = javaClass.classLoader!!
            .getResourceAsStream("cose-verification/$filename")!!
            .bufferedReader()
            .use { it.readText() }
            .trim()

        return hexString.chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }
}
