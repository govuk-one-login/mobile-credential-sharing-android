package uk.gov.onelogin.sharing.cryptoService.cbor.decoders.credential

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BinaryNode
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.dataformat.cbor.CBORParser
import com.fasterxml.jackson.core.JsonToken
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(AppScope::class)
class RawCredentialParserImpl : RawCredentialParser {

    override fun parse(rawCredential: ByteArray): ParsedRawCredential = try {
        val cborMapper = ObjectMapper(CBORFactory())
        val root = cborMapper.readTree(rawCredential)

        val nameSpacesNode = root.get(KEY_NAME_SPACES)
            ?: throw RawCredentialParsingException(ERROR_MISSING_NAMESPACES)
        val issuerAuthNode = root.get(KEY_ISSUER_AUTH) as? ArrayNode
            ?: throw RawCredentialParsingException(ERROR_MISSING_ISSUER_AUTH)

        val msoDocType = extractMsoDocType(cborMapper, issuerAuthNode)

        // Extract issuerAuth raw bytes directly from the credential to preserve
        // CBOR integer keys (Jackson's JsonNode converts them to strings)
        val issuerAuthBytes = extractIssuerAuthRawBytes(rawCredential)

        ParsedRawCredential(
            nameSpaces = cborMapper.writeValueAsBytes(nameSpacesNode),
            issuerAuth = issuerAuthBytes,
            msoDocType = msoDocType
        )
    } catch (e: RawCredentialParsingException) {
        throw e
    } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
        throw RawCredentialParsingException(FAILED_TO_PARSE_RAW_CREDENTIAL_CBOR, e)
    }

    /**
     * Extracts the raw CBOR bytes of the issuerAuth field, preserving the original
     * encoding including integer keys in nested maps.
     *
     * Uses the streaming parser to locate the field, then reads the raw value bytes
     * via [ObjectMapper] with the token buffer to get an exact CBOR re-encoding.
     * Since Jackson re-encoding loses integer keys, we instead locate the byte offset
     * and extract the raw slice from the input.
     */
    @Suppress("NestedBlockDepth")
    private fun extractIssuerAuthRawBytes(cborData: ByteArray): ByteArray {
        val factory = CBORFactory()
        val parser = factory.createParser(cborData) as CBORParser
        parser.use { p ->
            p.nextToken() // START_OBJECT (top-level map)
            while (p.nextToken() != null && p.currentToken() != JsonToken.END_OBJECT) {
                val name = p.currentName() ?: run { p.skipChildren(); continue }
                p.nextToken() // move to value

                if (name == KEY_ISSUER_AUTH) {
                    return extractCurrentValueBytes(p, cborData)
                }
                p.skipChildren()
            }
        }
        throw RawCredentialParsingException(ERROR_MISSING_ISSUER_AUTH)
    }

    private fun extractCurrentValueBytes(parser: CBORParser, source: ByteArray): ByteArray {
        val startOffset = parser.currentTokenLocation().byteOffset.toInt()
        parser.skipChildren()
        val endOffset = parser.currentLocation().byteOffset.toInt()
        return source.copyOfRange(startOffset, endOffset)
    }

    @Suppress("ThrowsCount")
    private fun extractMsoDocType(cborMapper: ObjectMapper, issuerAuthArray: ArrayNode): String {
        // COSE_Sign1: [protected, unprotected, payload, signature] - RFC 9052 standard
        val payloadNode = issuerAuthArray.get(COSE_SIGN1_PAYLOAD_INDEX) as? BinaryNode
            ?: throw RawCredentialParsingException(ERROR_MISSING_COSE_PAYLOAD)

        val payloadBytes = payloadNode.binaryValue()

        // Unwrap Tag 24: the outer CBOR is a tagged bytestring containing the MSO
        val innerTree = cborMapper.readTree(payloadBytes)
        val innerBytes = (innerTree as? BinaryNode)?.binaryValue()
            ?: throw RawCredentialParsingException(ERROR_INVALID_TAG24)

        val mobileSecurityObject = cborMapper.readTree(innerBytes)
        return mobileSecurityObject.get(KEY_DOC_TYPE)?.asText()
            ?: throw RawCredentialParsingException(ERROR_MISSING_DOCTYPE)
    }

    private companion object {
        const val KEY_NAME_SPACES = "nameSpaces"
        const val KEY_ISSUER_AUTH = "issuerAuth"
        const val KEY_DOC_TYPE = "docType"
        const val COSE_SIGN1_PAYLOAD_INDEX = 2
        const val FAILED_TO_PARSE_RAW_CREDENTIAL_CBOR = "Failed to parse raw credential CBOR"
        const val ERROR_MISSING_NAMESPACES = "Missing 'nameSpaces' in raw credential"
        const val ERROR_MISSING_ISSUER_AUTH = "Missing or invalid 'issuerAuth' in raw credential"
        const val ERROR_MISSING_COSE_PAYLOAD = "Missing COSE_Sign1 payload in issuerAuth"
        const val ERROR_INVALID_TAG24 = "Invalid Tag 24 wrapper in issuerAuth payload"
        const val ERROR_MISSING_DOCTYPE = "Missing 'docType' in MobileSecurityObject"
    }
}
