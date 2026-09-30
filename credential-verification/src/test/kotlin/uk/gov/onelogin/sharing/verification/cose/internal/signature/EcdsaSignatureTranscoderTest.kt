package uk.gov.onelogin.sharing.verification.cose.internal.signature

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Assert.assertThrows
import org.junit.Test
import uk.gov.onelogin.sharing.verification.cose.CoseVerificationFailure.InvalidSignature
import uk.gov.onelogin.sharing.verification.cose.internal.decode.CoseAlgorithm

class EcdsaSignatureTranscoderTest {

    @Test
    fun `throws when signature size is invalid`() {
        assertThrows(InvalidSignature::class.java) {
            EcdsaSignatureTranscoder.rawToDer(ByteArray(32))
        }
        assertThrows(InvalidSignature::class.java) {
            EcdsaSignatureTranscoder.rawToDer(ByteArray(80))
        }
    }

    @Test
    fun `throws when signature size does not match expected algorithm`() {
        assertThrows(InvalidSignature::class.java) {
            EcdsaSignatureTranscoder.rawToDer(ByteArray(96), CoseAlgorithm.ES256)
        }
        assertThrows(InvalidSignature::class.java) {
            EcdsaSignatureTranscoder.rawToDer(ByteArray(64), CoseAlgorithm.ES384)
        }
    }

    @Test
    fun `converts valid 64-byte signature to DER for ES256`() {
        val raw = ByteArray(64) { 0x01 }
        val der = EcdsaSignatureTranscoder.rawToDer(raw, CoseAlgorithm.ES256)

        // DER: SEQUENCE { INTEGER(r), INTEGER(s) }
        assertThat(der[0], equalTo(0x30.toByte()))
        assertThat(der[2], equalTo(0x02.toByte())) // first INTEGER tag
    }

    @Test
    fun `converts valid 96-byte signature to DER for ES384`() {
        val raw = ByteArray(96) { 0x01 }
        val der = EcdsaSignatureTranscoder.rawToDer(raw, CoseAlgorithm.ES384)

        // DER: SEQUENCE { INTEGER(r), INTEGER(s) }
        assertThat(der[0], equalTo(0x30.toByte()))
        assertThat(der[2], equalTo(0x02.toByte())) // first INTEGER tag
    }

    @Test
    fun `pads with leading zero when high bit is set`() {
        val raw = ByteArray(64) { 0xFF.toByte() }
        val der = EcdsaSignatureTranscoder.rawToDer(raw)

        // r is 32 bytes of 0xFF (no leading zeros to trim), padded with 0x00
        // INTEGER tag, length 33, 0x00, then 32 bytes of 0xFF
        assertThat(der[2], equalTo(0x02.toByte()))
        assertThat(der[3], equalTo(33.toByte()))
        assertThat(der[4], equalTo(0x00.toByte()))
        assertThat(der[5], equalTo(0xFF.toByte()))
    }

    @Test
    fun `trims leading zeros from components`() {
        val raw = ByteArray(64)
        raw[31] = 0x01 // r = 000...01
        raw[63] = 0x02 // s = 000...02
        val der = EcdsaSignatureTranscoder.rawToDer(raw)

        // r INTEGER: tag(02) len(01) value(01)
        assertThat(der[2], equalTo(0x02.toByte()))
        assertThat(der[3], equalTo(0x01.toByte()))
        assertThat(der[4], equalTo(0x01.toByte()))
    }
}
