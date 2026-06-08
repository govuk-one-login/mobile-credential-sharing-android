package uk.gov.onelogin.sharing.verification.trust

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BinaryNode
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import uk.gov.onelogin.sharing.verification.format.cose.CoseSign1
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult

internal object CoseSign1Decoder {
    private const val COSE_SIGN1_SIZE = 4
    private const val INDEX_PROTECTED = 0
    private const val INDEX_UNPROTECTED = 1
    private const val INDEX_PAYLOAD = 2
    private const val INDEX_SIGNATURE = 3
    private const val X5CHAIN_LABEL = 33

    private val cborMapper: ObjectMapper = ObjectMapper(CBORFactory())

    @Suppress("ThrowsCount")
    fun decode(data: ByteArray): CoseSign1 {
        val root = try {
            cborMapper.readTree(data) as? ArrayNode
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            println(e)
            null
        } ?: throw VerificationResult.Failure(VerificationError.MALFORMED_ISSUER_AUTH)

        if (root.size() != COSE_SIGN1_SIZE) {
            throw VerificationResult.Failure(VerificationError.MALFORMED_ISSUER_AUTH)
        }

        val protectedHeader = (root[INDEX_PROTECTED] as? BinaryNode)?.binaryValue()
            ?: throw VerificationResult.Failure(VerificationError.MALFORMED_ISSUER_AUTH)
        val unprotected = when (val node = root[INDEX_UNPROTECTED]) {
            is BinaryNode -> node.binaryValue()
            else -> cborMapper.writeValueAsBytes(node)
        }
        val payload = (root[INDEX_PAYLOAD] as? BinaryNode)?.binaryValue()
        val signature = (root[INDEX_SIGNATURE] as? BinaryNode)?.binaryValue()
            ?: throw VerificationResult.Failure(VerificationError.MALFORMED_ISSUER_AUTH)

        return CoseSign1(
            protectedHeader = protectedHeader,
            unprotectedHeader = unprotected,
            payload = payload,
            signature = signature
        )
    }

    fun extractX5Chain(coseSign1: CoseSign1): List<ByteArray> =
        coseSign1.unprotectedHeader?.let { extractX5ChainFromBytes(it) }
            ?: extractX5ChainFromBytes(coseSign1.protectedHeader)
            ?: throw VerificationResult.Failure(VerificationError.MALFORMED_ISSUER_AUTH)

    private fun extractX5ChainFromBytes(headerBytes: ByteArray): List<ByteArray>? {
        val node = try {
            cborMapper.readTree(headerBytes)
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            println(e)
            return null
        }
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
