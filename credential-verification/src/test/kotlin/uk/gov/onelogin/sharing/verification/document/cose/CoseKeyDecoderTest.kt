package uk.gov.onelogin.sharing.verification.document.cose

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import java.security.KeyPairGenerator
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResultMatchers.hasError

class CoseKeyDecoderTest {
    private val decoder = CoseKeyDecoder()
    private val cborMapper = ObjectMapper(CBORFactory())

    private fun buildValidCoseKey(): ByteArray {
        val keyPair = KeyPairGenerator.getInstance("EC")
            .apply { initialize(ECGenParameterSpec("secp256r1")) }
            .generateKeyPair()
        val pub = keyPair.public as ECPublicKey
        val x = pub.w.affineX.toByteArray().let { fixCoordinate(it) }
        val y = pub.w.affineY.toByteArray().let { fixCoordinate(it) }

        val node = cborMapper.createObjectNode()
        node.put("1", 2)   // kty = EC2
        node.put("-1", 1)  // crv = P-256
        node.put("-2", x)  // x
        node.put("-3", y)  // y
        return cborMapper.writeValueAsBytes(node)
    }

    private fun fixCoordinate(bytes: ByteArray): ByteArray {
        return when {
            bytes.size == 33 && bytes[0] == 0.toByte() -> bytes.copyOfRange(1, 33)
            bytes.size < 32 -> ByteArray(32 - bytes.size) + bytes
            else -> bytes
        }
    }

    @Test
    fun `decodes valid P-256 COSE_Key`() {
        val coseKeyBytes = buildValidCoseKey()
        val result = decoder.decode(coseKeyBytes)
        assertNotNull(result)
    }

    @Test
    fun `throws INVALID_DEVICE_KEY for extra labels`() {
        val keyPair = KeyPairGenerator.getInstance("EC")
            .apply { initialize(ECGenParameterSpec("secp256r1")) }
            .generateKeyPair()
        val pub = keyPair.public as ECPublicKey
        val x = fixCoordinate(pub.w.affineX.toByteArray())
        val y = fixCoordinate(pub.w.affineY.toByteArray())

        val node = cborMapper.createObjectNode()
        node.put("1", 2)
        node.put("-1", 1)
        node.put("-2", x)
        node.put("-3", y)
        node.put("2", 42)  // extra label (kid)

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            decoder.decode(cborMapper.writeValueAsBytes(node))
        }
        assertThat(exception, hasError(VerificationError.INVALID_DEVICE_KEY))
    }

    @Test
    fun `throws INVALID_DEVICE_KEY for wrong kty`() {
        val node = cborMapper.createObjectNode()
        node.put("1", 1)   // kty = OKP (not EC2)
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
        node.put("-1", 2)  // crv = P-384
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
