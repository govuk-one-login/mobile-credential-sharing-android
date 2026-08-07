package uk.gov.onelogin.sharing.cryptoService.cbor.decoders.credential

import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BinaryNode
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.dataformat.cbor.CBORParser
import uk.gov.onelogin.sharing.core.SharingSessionScope
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(SharingSessionScope::class)
class RawCredentialParserImpl : RawCredentialParser {

    override fun parse(rawCredential: ByteArray): ParsedRawCredential = try {
        val cborMapper = ObjectMapper(CBORFactory())
        val root = cborMapper.readTree(rawCredential)

        val nameSpacesNode = root.get(KEY_NAME_SPACES)
            ?: throw RawCredentialParsingException(ERROR_MISSING_NAMESPACES)
        val issuerAuthNode = root.get(KEY_ISSUER_AUTH) as? ArrayNode
            ?: throw RawCredentialParsingException(ERROR_MISSING_ISSUER_AUTH)

        val msoDocType = extractMsoDocType(cborMapper, issuerAuthNode)
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
     * Extracts the raw CBOR bytes of the issuerAuth field from the credential.
     *
     * Jackson library converts CBOR integer map keys to strings which produces
     * invalid COSE headers and breaks signature verification on the verifier.
     *
     * This uses the parser to locate the field by byte offset and extract the
     * original bytes without re-encoding.
     *
     */
    private fun extractIssuerAuthRawBytes(cborData: ByteArray): ByteArray {
        val factory = CBORFactory()
        val parser = factory.createParser(cborData) as CBORParser
        parser.use { p ->
            p.nextToken()
            while (p.nextToken() != null && p.currentToken() != JsonToken.END_OBJECT) {
                val name = p.currentName() ?: run {
                    p.skipChildren()
                    continue
                }
                p.nextToken()

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
