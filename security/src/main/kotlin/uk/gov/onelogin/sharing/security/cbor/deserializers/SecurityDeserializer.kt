package uk.gov.onelogin.sharing.security.cbor.deserializers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import uk.gov.onelogin.sharing.security.cbor.dto.CoseKeyDto
import uk.gov.onelogin.sharing.security.cbor.dto.SecurityDto

class SecurityDeserializer : JsonDeserializer<SecurityDto>() {
    private val cborFactory = CBORFactory()
    private val cborMapper = ObjectMapper(cborFactory)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): SecurityDto {
        val root = p.codec.readTree<JsonNode>(p)

        val cipherSuiteIdentifier = root[0].intValue()
        val eDeviceKeyBytes = root[1].binaryValue()

        val parser = cborFactory.createParser(eDeviceKeyBytes).apply {
            codec = cborMapper
        }
        // we need to ignore the embedded cbor tag (24) and skip over to the next element
        parser.nextToken()
        parser.nextToken()

        val coseNode = parser.codec.readTree<JsonNode>(parser)

        val cose = CoseKeyDto(
            keyType = coseNode["1"].longValue(),
            curve = coseNode["-1"].longValue(),
            x = coseNode["-2"].binaryValue(),
            y = coseNode["-3"].binaryValue()
        )

        return SecurityDto(
            cipherSuiteIdentifier,
            cose
        )
    }
}
