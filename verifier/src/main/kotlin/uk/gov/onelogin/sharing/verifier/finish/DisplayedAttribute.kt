package uk.gov.onelogin.sharing.verifier.finish

import com.fasterxml.jackson.databind.JsonNode
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMapper
import uk.gov.onelogin.sharing.models.mdoc.cbor.serializers.EmbeddedCbor
import uk.gov.onelogin.sharing.verification.format.document.VerifiableDocument

/**
 * A single decoded IssuerSignedItem, formatted for display.
 */
internal data class DisplayedAttribute(val elementIdentifier: String, val value: String)

/**
 * Decodes the Tag 24 encoded IssuerSignedItemBytes of this document into displayable
 * attributes, grouped by namespace.
 */
internal fun VerifiableDocument.toDisplayedAttributes(): Map<String, List<DisplayedAttribute>> =
    issuerSigned.nameSpaces.orEmpty().mapValues { (_, items) ->
        items.mapNotNull(::decodeIssuerSignedItem)
    }

private fun decodeIssuerSignedItem(itemBytes: ByteArray): DisplayedAttribute? = try {
    val inner = CborMapper.default.readValue(itemBytes, EmbeddedCbor::class.java).encoded
    val item = CborMapper.default.readTree(inner)
    DisplayedAttribute(
        elementIdentifier = item.get(ELEMENT_IDENTIFIER).asText(),
        value = item.get(ELEMENT_VALUE).toDisplayValue()
    )
} catch (@Suppress("TooGenericExceptionCaught") _: Exception) {
    null
}

private fun JsonNode?.toDisplayValue(): String = when {
    this == null || isNull -> ""
    isBinary -> "<${binaryValue().size} bytes>"
    isValueNode -> asText()
    else -> toString()
}

private const val ELEMENT_IDENTIFIER = "elementIdentifier"
private const val ELEMENT_VALUE = "elementValue"
