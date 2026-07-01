package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse

import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.dataformat.cbor.CBORParser

/**
 * Extracts the raw `IssuerSignedItemBytes` from CBOR-encoded `DeviceResponse` bytes using
 * streaming parsing with offset tracking.
 *
 * ISO section 8.3 requires that bytestrings are preserved exactly as received.
 * This extractor captures each Tag 24-wrapped item directly from the source bytes
 * without decoding or re-encoding.
 */
internal object IssuerSignedItemBytesExtractor {

    /**
     * @param source The full CBOR-encoded DeviceResponse bytes.
     * @return Raw IssuerSignedItemBytes per document, indexed by document order.
     */
    fun extract(source: ByteArray): List<Map<String, List<ByteArray>>> {
        val parser = CBORFactory().createParser(source) as CBORParser
        return parser.use { parseDocuments(it, source) }
    }

    private fun parseDocuments(
        parser: CBORParser,
        source: ByteArray
    ): List<Map<String, List<ByteArray>>> {
        val documents = mutableListOf<Map<String, List<ByteArray>>>()

        parser.nextToken() // START_OBJECT (DeviceResponse)
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            val fieldName = parser.currentName()
            parser.nextToken()

            if (fieldName == KEY_DOCUMENTS) {
                while (parser.nextToken() != JsonToken.END_ARRAY) {
                    documents.add(extractDocumentNameSpaces(parser, source))
                }
            } else {
                parser.skipChildren()
            }
        }
        return documents
    }

    private fun extractDocumentNameSpaces(
        parser: CBORParser,
        source: ByteArray
    ): Map<String, List<ByteArray>> {
        var nameSpaces: Map<String, List<ByteArray>> = emptyMap()

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            val fieldName = parser.currentName()
            parser.nextToken()

            if (fieldName == KEY_ISSUER_SIGNED) {
                nameSpaces = extractIssuerSignedNameSpaces(parser, source)
            } else {
                parser.skipChildren()
            }
        }
        return nameSpaces
    }

    private fun extractIssuerSignedNameSpaces(
        parser: CBORParser,
        source: ByteArray
    ): Map<String, List<ByteArray>> {
        var nameSpaces: Map<String, List<ByteArray>> = emptyMap()

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            val fieldName = parser.currentName()
            parser.nextToken()

            if (fieldName == KEY_NAME_SPACES) {
                nameSpaces = extractNameSpaces(parser, source)
            } else {
                parser.skipChildren()
            }
        }
        return nameSpaces
    }

    private fun extractNameSpaces(
        parser: CBORParser,
        source: ByteArray
    ): Map<String, List<ByteArray>> {
        val result = mutableMapOf<String, List<ByteArray>>()

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            val nameSpace = parser.currentName()
            parser.nextToken() // START_ARRAY

            val items = mutableListOf<ByteArray>()
            while (parser.nextToken() != JsonToken.END_ARRAY) {
                val startOffset = parser.currentTokenLocation().byteOffset.toInt()
                parser.binaryValue
                val endOffset = parser.currentLocation().byteOffset.toInt()
                items.add(source.copyOfRange(startOffset, endOffset))
            }
            result[nameSpace] = items
        }
        return result
    }

    private const val KEY_DOCUMENTS = "documents"
    private const val KEY_ISSUER_SIGNED = "issuerSigned"
    private const val KEY_NAME_SPACES = "nameSpaces"
}
