package uk.gov.onelogin.sharing.verification.document.cose

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import java.security.KeyPairGenerator
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Test
import uk.gov.onelogin.sharing.verification.cose.CoseSign1Stubs.coseKeyBytes
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResultMatchers.hasError

class CoseKeyDecoderTest {
    private val decoder = CoseKeyDecoder()
    private val cborMapper = ObjectMapper(CBORFactory())

    private val keyPair = KeyPairGenerator.getInstance("EC")
        .apply { initialize(ECGenParameterSpec("secp256r1")) }
        .generateKeyPair()
    private val publicKey = keyPair.public as ECPublicKey

    @Test
    fun `decodes valid P-256 COSE_Key`() {
        val result = decoder.decode(coseKeyBytes(publicKey))
        assertNotNull(result)
    }

    @Test
    fun `throws INVALID_DEVICE_KEY for extra labels`() {
        val node = cborMapper.createObjectNode()
        node.put("1", 2)
        node.put("-1", 1)
        node.put("-2", ByteArray(32))
        node.put("-3", ByteArray(32))
        node.put("2", 42) // extra label (kid)

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            decoder.decode(cborMapper.writeValueAsBytes(node))
        }
        assertThat(exception, hasError(VerificationError.INVALID_DEVICE_KEY))
    }

    @Test
    fun `throws INVALID_DEVICE_KEY for wrong kty`() {
        val node = cborMapper.createObjectNode()
        node.put("1", 1) // kty = OKP (not EC2)
        node.put("-1", 1)
        node.put("-2", ByteArray(32))
        node.put("-3", ByteArray(32))

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            decoder.decode(cborMapper.writeValueAsBytes(node))
        }
        assertThat(exception, hasError(VerificationError.INVALID_DEVICE_KEY))
    }

    @Test
    fun `throws INVALID_DEVICE_KEY for wrong crv`() {
        val node = cborMapper.createObjectNode()
        node.put("1", 2)
        node.put("-1", 2) // crv = P-384
        node.put("-2", ByteArray(32))
        node.put("-3", ByteArray(32))

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            decoder.decode(cborMapper.writeValueAsBytes(node))
        }
        assertThat(exception, hasError(VerificationError.INVALID_DEVICE_KEY))
    }

    @Test
    fun `throws INVALID_DEVICE_KEY for malformed bytes`() {
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            decoder.decode(byteArrayOf(0xFF.toByte(), 0xFE.toByte()))
        }
        assertThat(exception, hasError(VerificationError.INVALID_DEVICE_KEY))
    }

    @Test
    fun `throws INVALID_DEVICE_KEY for missing y coordinate`() {
        val node = cborMapper.createObjectNode()
        node.put("1", 2)
        node.put("-1", 1)
        node.put("-2", ByteArray(32))
        // missing -3 (y)

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            decoder.decode(cborMapper.writeValueAsBytes(node))
        }
        assertThat(exception, hasError(VerificationError.INVALID_DEVICE_KEY))
    }
}
