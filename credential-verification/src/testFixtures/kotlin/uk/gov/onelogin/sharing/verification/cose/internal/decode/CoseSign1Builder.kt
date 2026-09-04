package uk.gov.onelogin.sharing.verification.cose.internal.decode

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import java.security.KeyPair
import java.security.MessageDigest
import java.security.Signature
import java.security.cert.X509Certificate

/**
 * Builds valid COSE_Sign1 CBOR structures for testing.
 * Produces a real ECDSA signature over the Sig_structure.
 */
object CoseSign1Builder {
    private val cborFactory = CBORFactory()
    private val cborMapper = ObjectMapper(cborFactory)

    const val X5CHAIN_LABEL = "33"
    const val X5T_LABEL = "34"
    const val X5BAG_LABEL = "32"
    const val COSE_ALG_SHA_256 = -16

    fun build(chain: List<X509Certificate>, leafKeyPair: KeyPair, payload: ByteArray): ByteArray {
        val protectedHeader = buildProtectedHeader(chain)
        val unprotectedHeader = buildUnprotectedHeader(chain)
        val signature = sign(protectedHeader, payload, leafKeyPair)

        return assemble(protectedHeader, cborMapper.readTree(unprotectedHeader), payload, signature)
    }

    /**
     * Protected header CBOR map: `{1: -7}` (ES256) plus a SHA-256 `x5t` bound to the first
     * certificate. Callers may override individual entries via [protectedOverrides].
     */
    fun protectedHeaderBytes(
        chain: List<X509Certificate>,
        includeX5t: Boolean = true,
        protectedOverrides: ObjectNode.() -> Unit = {}
    ): ByteArray {
        val node = cborMapper.createObjectNode()
        node.put("1", -7)
        if (includeX5t && chain.isNotEmpty()) {
            node.set<ArrayNode>(X5T_LABEL, sha256X5t(chain.first()))
        }
        node.protectedOverrides()
        return cborMapper.writeValueAsBytes(node)
    }

    /** Unprotected header CBOR map carrying leaf-first `x5chain`. */
    fun unprotectedHeaderBytes(
        chain: List<X509Certificate>,
        unprotectedOverrides: ObjectNode.() -> Unit = {}
    ): ByteArray {
        val node = cborMapper.createObjectNode()
        putX5Chain(node, chain)
        node.unprotectedOverrides()
        return cborMapper.writeValueAsBytes(node)
    }

    fun putX5Chain(node: ObjectNode, chain: List<X509Certificate>) {
        if (chain.size == 1) {
            node.put(X5CHAIN_LABEL, chain[0].encoded)
        } else {
            val arr = cborMapper.createArrayNode()
            chain.forEach { arr.add(it.encoded) }
            node.set<ArrayNode>(X5CHAIN_LABEL, arr)
        }
    }

    /** A `[SHA-256 (-16), 32-byte digest]` x5t node for the supplied certificate. */
    fun sha256X5t(cert: X509Certificate): ArrayNode {
        val digest = MessageDigest.getInstance("SHA-256").digest(cert.encoded)
        return x5tNode(COSE_ALG_SHA_256, digest)
    }

    /** A x5t node with an arbitrary algorithm identifier and hash bytes. */
    fun x5tNode(alg: Int, hash: ByteArray): ArrayNode {
        val arr = cborMapper.createArrayNode()
        arr.add(alg)
        arr.add(hash)
        return arr
    }

    fun objectNode(): ObjectNode = cborMapper.createObjectNode()

    fun toBytes(node: ObjectNode): ByteArray = cborMapper.writeValueAsBytes(node)

    private fun assemble(
        protectedHeader: ByteArray,
        unprotectedNode: com.fasterxml.jackson.databind.JsonNode,
        payload: ByteArray?,
        signature: ByteArray
    ): ByteArray {
        val array = cborMapper.createArrayNode()
        array.add(protectedHeader)
        array.add(unprotectedNode)
        if (payload != null) array.add(payload) else array.addNull()
        array.add(signature)
        return cborMapper.writeValueAsBytes(array)
    }

    private fun buildProtectedHeader(chain: List<X509Certificate>): ByteArray =
        protectedHeaderBytes(chain)

    private fun buildUnprotectedHeader(chain: List<X509Certificate>): ByteArray =
        unprotectedHeaderBytes(chain)

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
