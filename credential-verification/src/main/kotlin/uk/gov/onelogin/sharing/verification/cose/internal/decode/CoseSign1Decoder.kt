package uk.gov.onelogin.sharing.verification.cose.internal.decode

import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BinaryNode
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.dataformat.cbor.CBORParser
import dev.zacsweers.metro.Inject
import uk.gov.onelogin.sharing.verification.cose.CoseVerificationFailure.MalformedCoseSign1
import uk.gov.onelogin.sharing.verification.cose.CoseVerificationFailure.MissingX5Chain

/**
 * Strict decoder for COSE_Sign1 structures as defined in ISO 18013-5.
 *
 * Implements byte-preservation using Jackson streaming to ensure cryptographic
 * signatures are verified against identical source bytes.
 */
@Inject
internal class CoseSign1Decoder {

    private val cborFactory = CBORFactory()
    private val mapper = ObjectMapper(cborFactory)

    /**
     * Decodes a COSE_Sign1 structure from raw bytes.
     *
     * @param data The raw CBOR bytes.
     * @return An [InternalCoseSign1] with preserved raw byte segments.
     * @throws MalformedCoseSign1 if structure is not exactly a 4-element array or has tags.
     */
    @Suppress("ThrowsCount", "NestedBlockDepth", "DEPRECATION", "CyclomaticComplexMethod")
    fun decode(data: ByteArray): InternalCoseSign1 {
        if (data.isEmpty()) throw MalformedCoseSign1

        val firstByte = data[0].toInt() and BYTE_MASK
        if (firstByte in CBOR_TAG_RANGE_START..CBOR_TAG_RANGE_END) {
            throw MalformedCoseSign1
        }

        try {
            return (cborFactory.createParser(data) as CBORParser).use { parser ->
                if (parser.nextToken() != JsonToken.START_ARRAY) {
                    throw MalformedCoseSign1
                }

                val protectedHeader = if (parser.nextToken() == JsonToken.VALUE_EMBEDDED_OBJECT) {
                    parser.binaryValue
                } else {
                    throw MalformedCoseSign1
                }

                if (parser.nextToken() != JsonToken.START_OBJECT) {
                    throw MalformedCoseSign1
                }
                val unprotectedStart = parser.tokenLocation.byteOffset.toInt()
                parser.skipChildren()
                val unprotectedEnd = parser.currentLocation.byteOffset.toInt()
                val unprotectedHeader = data.copyOfRange(unprotectedStart, unprotectedEnd)

                val payloadToken = parser.nextToken()
                val (payload, mode) = when (payloadToken) {
                    JsonToken.VALUE_EMBEDDED_OBJECT ->
                        parser.binaryValue to InternalCoseSign1.PayloadMode.ATTACHED

                    JsonToken.VALUE_NULL -> null to InternalCoseSign1.PayloadMode.DETACHED

                    else -> throw MalformedCoseSign1
                }

                val signature = if (parser.nextToken() == JsonToken.VALUE_EMBEDDED_OBJECT) {
                    parser.binaryValue
                } else {
                    throw MalformedCoseSign1
                }

                if (parser.parsingContext.entryCount != COSE_SIGN1_SIZE) {
                    throw MalformedCoseSign1
                }

                if (parser.nextToken() != JsonToken.END_ARRAY || parser.nextToken() != null) {
                    throw MalformedCoseSign1
                }

                InternalCoseSign1(
                    protectedHeader = protectedHeader,
                    unprotectedHeader = unprotectedHeader,
                    payload = payload,
                    signature = signature,
                    payloadMode = mode
                )
            }
        } catch (@Suppress("TooGenericExceptionCaught") _: Exception) {
            @Suppress("SwallowedException")
            throw MalformedCoseSign1
        }
    }

    /**
     * Extracts x5chain certificates from the COSE headers.
     */
    fun extractX5Chain(coseSign1: InternalCoseSign1): List<ByteArray> =
        coseSign1.unprotectedHeader?.let { extractX5ChainFromBytes(it) }
            ?: extractX5ChainFromBytes(coseSign1.protectedHeader)
            ?: throw MissingX5Chain

    private fun extractX5ChainFromBytes(headerBytes: ByteArray): List<ByteArray>? {
        val node = try {
            mapper.readTree(headerBytes)
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

    private companion object {
        const val COSE_SIGN1_SIZE = 4
        const val X5CHAIN_LABEL = 33
        const val CBOR_TAG_RANGE_START = 0xC0
        const val CBOR_TAG_RANGE_END = 0xDF
        const val BYTE_MASK = 0xFF
    }
}
