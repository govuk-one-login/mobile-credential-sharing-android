package uk.gov.onelogin.sharing.security.cbor.deserializers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import uk.gov.onelogin.sharing.security.cbor.dto.SessionEstablishmentDto
import uk.gov.onelogin.sharing.security.cbor.serializers.EmbeddedCbor

class SessionEstablishmentDeserializer : JsonDeserializer<SessionEstablishmentDto>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): SessionEstablishmentDto {
        val root = p.codec.readTree<JsonNode>(p)

        require(root.isObject) { "Expected a CBOR map object" }

        val eReaderKeyNode = root["eReaderKey"]
        requireNotNull(eReaderKeyNode) { "Missing required field: 'eReaderKey'" }

        val dataNode = root["data"]
        requireNotNull(dataNode) { "Missing required field: 'data'" }

        val eReaderKeyBytes = eReaderKeyNode.binaryValue()
        val dataBytes = dataNode.binaryValue()

        return SessionEstablishmentDto(
            eReaderKey = EmbeddedCbor(eReaderKeyBytes),
            data = dataBytes
        )
    }
}
