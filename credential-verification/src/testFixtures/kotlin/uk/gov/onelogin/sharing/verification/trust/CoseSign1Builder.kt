package uk.gov.onelogin.sharing.verification.trust

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import java.security.KeyPair
import java.security.Signature
import java.security.cert.X509Certificate

/**
 * Builds valid COSE_Sign1 CBOR structures for testing.
 * Produces a real ECDSA signature over the Sig_structure.
 */
object CoseSign1Builder {
    private val cborFactory = CBORFactory()
    private val cborMapper = ObjectMapper(cborFactory)

    fun build(chain: List<X509Certificate>, leafKeyPair: KeyPair, payload: ByteArray): ByteArray {
        val protectedHeader = buildProtectedHeader()
        val unprotectedHeader = buildUnprotectedHeader(chain)
        val signature = sign(protectedHeader, payload, leafKeyPair)

        val array = cborMapper.createArrayNode()
        array.add(protectedHeader)
        array.add(cborMapper.readTree(unprotectedHeader))
        array.add(payload)
        array.add(signature)

        return cborMapper.writeValueAsBytes(array)
    }

    private fun buildProtectedHeader(): ByteArray {
        // {1: -7} = ES256
        val node = cborMapper.createObjectNode()
        node.put("1", -7)
        return cborMapper.writeValueAsBytes(node)
    }

    private fun buildUnprotectedHeader(chain: List<X509Certificate>): ByteArray {
        val node = cborMapper.createObjectNode()
        if (chain.size == 1) {
            node.put("33", chain[0].encoded)
        } else {
            val arr = cborMapper.createArrayNode()
            chain.forEach { arr.add(it.encoded) }
            node.set<ArrayNode>("33", arr)
        }
        return cborMapper.writeValueAsBytes(node)
    }

    private fun sign(protectedHeader: ByteArray, payload: ByteArray, keyPair: KeyPair): ByteArray {
        val sigStructure = buildSigStructure(protectedHeader, payload)
        val sig = Signature.getInstance("SHA256withECDSA")
        sig.initSign(keyPair.private)
        sig.update(sigStructure)
        val derSig = sig.sign()
        return derToRaw(derSig)
    }

    private fun buildSigStructure(protectedHeader: ByteArray, payload: ByteArray): ByteArray {
        val array = cborMapper.createArrayNode()
        array.add("Signature1")
        array.add(protectedHeader)
        array.add(byteArrayOf())
        array.add(payload)
        return cborMapper.writeValueAsBytes(array)
    }

    /**
     * Converts DER-encoded ECDSA signature to raw (r || s) format expected by COSE.
     */
    private fun derToRaw(der: ByteArray): ByteArray {
        var offset = 2
        val rLen = der[offset + 1].toInt() and 0xFF
        offset += 2
        val r = der.copyOfRange(offset, offset + rLen)
        offset += rLen
        val sLen = der[offset + 1].toInt() and 0xFF
        offset += 2
        val s = der.copyOfRange(offset, offset + sLen)

        return padOrTrim(r) + padOrTrim(s)
    }

    private fun padOrTrim(bytes: ByteArray): ByteArray = when {
        bytes.size == P256_COMPONENT_SIZE + 1 && bytes[0] == 0.toByte() ->
            bytes.copyOfRange(1, bytes.size)

        bytes.size < P256_COMPONENT_SIZE -> ByteArray(P256_COMPONENT_SIZE - bytes.size) + bytes

        else -> bytes.copyOfRange(bytes.size - P256_COMPONENT_SIZE, bytes.size)
    }

    private const val P256_COMPONENT_SIZE = 32
}
