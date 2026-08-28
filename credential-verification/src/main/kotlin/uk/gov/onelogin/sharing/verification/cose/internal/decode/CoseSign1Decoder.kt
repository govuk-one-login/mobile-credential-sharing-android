package uk.gov.onelogin.sharing.verification.cose.internal.decode

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BinaryNode
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import dev.zacsweers.metro.Inject
import uk.gov.onelogin.sharing.verification.cose.CoseVerificationFailure.MalformedCoseSign1

@Inject
internal class CoseSign1Decoder {

    private val cborMapper: ObjectMapper = ObjectMapper(CBORFactory())

    companion object {
        private const val COSE_SIGN1_SIZE = 4
        private const val INDEX_PROTECTED = 0
        private const val INDEX_UNPROTECTED = 1
        private const val INDEX_PAYLOAD = 2
        private const val INDEX_SIGNATURE = 3
        private const val X5CHAIN_LABEL = 33
    }

    @Suppress("ThrowsCount")
    fun decode(data: ByteArray): InternalCoseSign1 {
        val root = try {
            cborMapper.readTree(data) as? ArrayNode
        } catch (@Suppress("TooGenericExceptionCaught") _: Exception) {
            null
        } ?: throw MalformedCoseSign1

        if (root.size() != COSE_SIGN1_SIZE) {
            throw MalformedCoseSign1
        }

        val protectedHeader = (root[INDEX_PROTECTED] as? BinaryNode)?.binaryValue()
            ?: throw MalformedCoseSign1
        val unprotected = when (val node = root[INDEX_UNPROTECTED]) {
            is BinaryNode -> node.binaryValue()
            else -> cborMapper.writeValueAsBytes(node)
        }
        val payload = (root[INDEX_PAYLOAD] as? BinaryNode)?.binaryValue()
        val signature = (root[INDEX_SIGNATURE] as? BinaryNode)?.binaryValue()
            ?: throw MalformedCoseSign1

        return InternalCoseSign1(
            protectedHeader = protectedHeader,
            unprotectedHeader = unprotected,
            payload = payload,
            signature = signature
        )
    }

    fun extractX5Chain(coseSign1: InternalCoseSign1): List<ByteArray> =
        coseSign1.unprotectedHeader?.let { extractX5ChainFromBytes(it) }
            ?: extractX5ChainFromBytes(coseSign1.protectedHeader)
            ?: throw MalformedCoseSign1

    private fun extractX5ChainFromBytes(headerBytes: ByteArray): List<ByteArray>? {
        val node = try {
            cborMapper.readTree(headerBytes)
        } catch (@Suppress("TooGenericExceptionCaught") _: Exception) {
            null
        } ?: return null
        return extractX5ChainFromNode(node)
    }

    private fun extractX5ChainFromNode(node: JsonNode): List<ByteArray>? {
        val x5chainNode = node.get(X5CHAIN_LABEL.toString()) ?: return null
        return when {
            x5chainNode is BinaryNode -> listOf(x5chainNode.binaryValue())

            x5chainNode is ArrayNode -> x5chainNode.mapNotNull { element ->
                (element as? BinaryNode)?.binaryValue()
            }.ifEmpty { null }

            else -> null
        }
    }
}
