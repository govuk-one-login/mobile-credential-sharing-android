package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse

import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.dataformat.cbor.CBORParser

/**
 * Extracts raw CBOR bytes from a `DeviceResponse`
 *
 * ISO 18013-5 8.3 requires that structures with a "Bytes" suffix are preserved exactly as
 * received. This extractor captures Tag 24-wrapped items directly from the source bytes
 * without decoding or re-encoding.
 */
internal object RawBytesExtractor {

    /**
     * Per-document extraction result containing the raw bytes that must be preserved.
     */
    data class DocumentRawBytes(
        val issuerSignedItemBytes: Map<String, List<ByteArray>> = emptyMap(),
        val deviceNameSpacesBytes: ByteArray? = null,
        val deviceSignatureBytes: ByteArray? = null
    )

    /**
     * @param source The full CBOR-encoded DeviceResponse bytes.
     * @return Raw bytes per document, indexed by document order.
     */
    fun extract(source: ByteArray): List<DocumentRawBytes> {
        val parser = CBORFactory().createParser(source) as CBORParser
        return parser.use { parseDocuments(it, source) }
    }

    private fun parseDocuments(parser: CBORParser, source: ByteArray): List<DocumentRawBytes> {
        val documents = mutableListOf<DocumentRawBytes>()

        parser.nextToken() // START_OBJECT (DeviceResponse)
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            val fieldName = parser.currentName()
            parser.nextToken()

            if (fieldName == KEY_DOCUMENTS) {
                while (parser.nextToken() != JsonToken.END_ARRAY) {
                    documents.add(extractDocument(parser, source))
                }
            } else {
                parser.skipChildren()
            }
        }
        return documents
    }

    private fun extractDocument(parser: CBORParser, source: ByteArray): DocumentRawBytes {
        var issuerNameSpaces: Map<String, List<ByteArray>> = emptyMap()
        var deviceSignedRawBytes: DeviceSignedRawBytes? = null

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            val fieldName = parser.currentName()
            parser.nextToken()

            when (fieldName) {
                KEY_ISSUER_SIGNED ->
                    issuerNameSpaces =
                        extractIssuerSignedNameSpaces(parser, source)

                KEY_DEVICE_SIGNED -> deviceSignedRawBytes = extractDeviceSignedBytes(parser, source)

                else -> parser.skipChildren()
            }
        }
        return DocumentRawBytes(
            issuerSignedItemBytes = issuerNameSpaces,
            deviceNameSpacesBytes = deviceSignedRawBytes?.nameSpacesBytes,
            deviceSignatureBytes = deviceSignedRawBytes?.signatureBytes
        )
    }

    private data class DeviceSignedRawBytes(
        val nameSpacesBytes: ByteArray? = null,
        val signatureBytes: ByteArray? = null
    )

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

    private fun extractDeviceSignedBytes(
        parser: CBORParser,
        source: ByteArray
    ): DeviceSignedRawBytes {
        var nameSpacesBytes: ByteArray? = null
        var signatureBytes: ByteArray? = null

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            val fieldName = parser.currentName()
            parser.nextToken()

            when (fieldName) {
                KEY_NAME_SPACES -> {
                    val startOffset = parser.currentTokenLocation().byteOffset.toInt()
                    parser.binaryValue
                    val endOffset = parser.currentLocation().byteOffset.toInt()
                    nameSpacesBytes = source.copyOfRange(startOffset, endOffset)
                }

                KEY_DEVICE_AUTH -> {
                    signatureBytes = extractDeviceSignatureBytes(parser, source)
                }

                else -> parser.skipChildren()
            }
        }
        return DeviceSignedRawBytes(
            nameSpacesBytes = nameSpacesBytes,
            signatureBytes = signatureBytes
        )
    }

    private fun extractDeviceSignatureBytes(parser: CBORParser, source: ByteArray): ByteArray? {
        var bytes: ByteArray? = null

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            val fieldName = parser.currentName()
            parser.nextToken()

            if (fieldName == KEY_DEVICE_SIGNATURE) {
                val startOffset = parser.currentTokenLocation().byteOffset.toInt()
                parser.skipChildren()
                val endOffset = parser.currentLocation().byteOffset.toInt()
                bytes = source.copyOfRange(startOffset, endOffset)
            } else {
                parser.skipChildren()
            }
        }
        return bytes
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
    private const val KEY_DEVICE_SIGNED = "deviceSigned"
    private const val KEY_NAME_SPACES = "nameSpaces"
    private const val KEY_DEVICE_AUTH = "deviceAuth"
    private const val KEY_DEVICE_SIGNATURE = "deviceSignature"
}
