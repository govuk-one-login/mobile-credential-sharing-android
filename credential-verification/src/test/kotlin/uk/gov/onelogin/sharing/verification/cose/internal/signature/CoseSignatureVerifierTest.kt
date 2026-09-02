package uk.gov.onelogin.sharing.verification.cose.internal.signature

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import java.security.KeyPairGenerator
import java.security.Signature
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import org.junit.Assert.assertThrows
import org.junit.Test
import uk.gov.onelogin.sharing.verification.cose.CoseVerificationFailure.InvalidSignature
import uk.gov.onelogin.sharing.verification.cose.internal.decode.CoseHeaderValidator
import uk.gov.onelogin.sharing.verification.cose.internal.decode.InternalCoseSign1

class CoseSignatureVerifierTest {
    private val verifier =
        CoseSignatureVerifier(CoseHeaderValidator())
    private val cborMapper = ObjectMapper(CBORFactory())

    private val keyPair = KeyPairGenerator.getInstance("EC")
        .apply { initialize(ECGenParameterSpec("secp256r1")) }
        .generateKeyPair()
    private val publicKey = keyPair.public as ECPublicKey

    private fun buildProtectedHeader(): ByteArray {
        val node = cborMapper.createObjectNode()
        node.put("1", -7L)
        return cborMapper.writeValueAsBytes(node)
    }

    private fun buildEmptyMap(): ByteArray =
        cborMapper.writeValueAsBytes(cborMapper.createObjectNode())

    private fun sign(payload: ByteArray, protectedHeader: ByteArray): ByteArray {
        val sigStructure = verifier.buildSigStructure(protectedHeader, payload)
        val sig = Signature.getInstance("SHA256withECDSA")
        sig.initSign(keyPair.private)
        sig.update(sigStructure)
        return derToRaw(sig.sign())
    }

    private fun derToRaw(der: ByteArray): ByteArray {
        var offset = 2
        offset++
        val rLen = der[offset].toInt() and 0xFF
        offset++
        val rBytes = der.copyOfRange(offset, offset + rLen)
        offset += rLen
        offset++
        val sLen = der[offset].toInt() and 0xFF
        offset++
        val sBytes = der.copyOfRange(offset, offset + sLen)

        val raw = ByteArray(64)
        val rTrimmed = if (rBytes.size > 32) {
            rBytes.copyOfRange(rBytes.size - 32, rBytes.size)
        } else {
            rBytes
        }
        val sTrimmed = if (sBytes.size > 32) {
            sBytes.copyOfRange(sBytes.size - 32, sBytes.size)
        } else {
            sBytes
        }
        rTrimmed.copyInto(raw, 32 - rTrimmed.size)
        sTrimmed.copyInto(raw, 64 - sTrimmed.size)
        return raw
    }

    @Test
    fun `verify succeeds with valid signature`() {
        val protectedHeader = buildProtectedHeader()
        val payload = "test payload".toByteArray()
        val signature = sign(payload, protectedHeader)

        val coseSign1 = InternalCoseSign1(protectedHeader, buildEmptyMap(), payload, signature)

        verifier.verify(coseSign1, publicKey, payload)
    }

    @Test
    fun `verify throws with tampered signature`() {
        val protectedHeader = buildProtectedHeader()
        val payload = "test payload".toByteArray()
        val signature = sign(payload, protectedHeader)
        signature[0] = (signature[0].toInt() xor 0xFF).toByte()

        val coseSign1 = InternalCoseSign1(protectedHeader, buildEmptyMap(), payload, signature)

        assertThrows(InvalidSignature::class.java) {
            verifier.verify(coseSign1, publicKey, payload)
        }
    }

    @Test
    fun `verify throws with wrong public key`() {
        val protectedHeader = buildProtectedHeader()
        val payload = "test payload".toByteArray()
        val signature = sign(payload, protectedHeader)

        val otherKey = KeyPairGenerator.getInstance("EC")
            .apply { initialize(ECGenParameterSpec("secp256r1")) }
            .generateKeyPair().public as ECPublicKey

        val coseSign1 = InternalCoseSign1(protectedHeader, buildEmptyMap(), payload, signature)

        assertThrows(InvalidSignature::class.java) {
            verifier.verify(coseSign1, otherKey, payload)
        }
    }
}
