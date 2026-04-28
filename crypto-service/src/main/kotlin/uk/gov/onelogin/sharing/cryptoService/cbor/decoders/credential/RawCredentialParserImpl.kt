package uk.gov.onelogin.sharing.cryptoService.cbor.decoders.credential

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BinaryNode
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
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

        ParsedRawCredential(
            nameSpaces = cborMapper.writeValueAsBytes(nameSpacesNode),
            issuerAuth = cborMapper.writeValueAsBytes(issuerAuthNode),
            msoDocType = msoDocType
        )
    } catch (e: RawCredentialParsingException) {
        throw e
    } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
        throw RawCredentialParsingException(FAILED_TO_PARSE_RAW_CREDENTIAL_CBOR, e)
    }

    private fun extractMsoDocType(
        cborMapper: ObjectMapper,
        issuerAuthArray: ArrayNode
    ): String {
        // COSE_Sign1: [protectedHeader, unprotectedHeader, payload, signature]
        val payloadNode = issuerAuthArray.get(COSE_SIGN1_PAYLOAD_INDEX) as? BinaryNode
            ?: throw RawCredentialParsingException(ERROR_MISSING_COSE_PAYLOAD)

        val payloadBytes = payloadNode.binaryValue()

        // Unwrap Tag 24: the outer CBOR is a tagged bytestring containing the MSO
        val innerTree = cborMapper.readTree(payloadBytes)
        val innerBytes = (innerTree as? BinaryNode)?.binaryValue()
            ?: throw RawCredentialParsingException(ERROR_INVALID_TAG24)

        val mso = cborMapper.readTree(innerBytes)
        return mso.get(KEY_DOC_TYPE)?.asText()
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
