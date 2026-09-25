package uk.gov.onelogin.sharing.cryptoService.cbor.decoders.credential

import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.dataformat.cbor.CBORParser
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import kotlin.collections.isNotEmpty
import kotlin.collections.map
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.SharingIssuerSigned
import uk.gov.onelogin.sharing.verification.format.document.IssuerSigned

@Inject
@ContributesBinding(scope = AppScope::class, binding = binding<FilterIssuerSignedUseCase>())
class FilterIssuerSignedUseCaseImpl(private val logger: Logger) : FilterIssuerSignedUseCase {

    private val cborMapper = ObjectMapper(CBORFactory())

    override fun filter(
        validatedCredential: ParsedRawCredential,
        deviceRequest: DeviceRequest
    ): FilteredIssuerSigned {
        val ageOverNNCount = deviceRequest.docRequests.sumOf { docRequest ->
            docRequest.itemsRequest.nameSpaces.values.sumOf { elements ->
                elements.keys.count { isAgeOverNN(it) }
            }
        }

        if (ageOverNNCount > 2) {
            logger.debug(logTag, AgeOverNNRequestLimitException.MESSAGE)
            throw AgeOverNNRequestLimitException()
        }

        val requestedNameSpaces = deviceRequest.docRequests
            .firstOrNull()?.itemsRequest?.nameSpaces
            ?: throw NoMatchingAttributesException(LOG_NO_MATCHING_NAMESPACES)

        val credentialNameSpaces = parseNameSpaces(validatedCredential.nameSpaces)

        val filteredNameSpaces = mutableMapOf<String, List<ByteArray>>()
        val matchedAttributes = mutableMapOf<String, List<MatchedAttribute>>()

        for ((nameSpace, requestedElements) in requestedNameSpaces) {
            val credentialItems = credentialNameSpaces[nameSpace] ?: continue
            val matchedItems = filterItems(credentialItems, requestedElements)
            if (matchedItems.isNotEmpty()) {
                filteredNameSpaces[nameSpace] = matchedItems.map { it.bytes }
                matchedAttributes[nameSpace] = matchedItems.map {
                    MatchedAttribute(it.identifier, it.intentToRetain)
                }
            }
        }

        if (filteredNameSpaces.isEmpty()) {
            val hasMatchingNameSpace = requestedNameSpaces.keys.any { it in credentialNameSpaces }
            val message = if (hasMatchingNameSpace) {
                LOG_NO_MATCHING_ATTRIBUTES
            } else {
                LOG_NO_MATCHING_NAMESPACES
            }
            logger.debug(logTag, message)
            throw NoMatchingAttributesException(message)
        }

        logger.debug(logTag, "nameSpaces: ${filteredNameSpaces.keys}")

        val issuerSigned = SharingIssuerSigned(
            nameSpaces = filteredNameSpaces,
            issuerAuth = validatedCredential.issuerAuth
        )
        logger.debug(logTag, "IssuerSigned assembled with ${filteredNameSpaces.size} namespace(s)")
        return FilteredIssuerSigned(
            issuerSigned = issuerSigned,
            matchedAttributes = matchedAttributes
        )
    }

    /**
     * Parses the raw nameSpaces CBOR bytes into a map of namespace -> list of inner item bytes.
     */
    private fun parseNameSpaces(nameSpacesBytes: ByteArray): Map<String, List<ByteArray>> {
        val result = mutableMapOf<String, List<ByteArray>>()
        val parser = cborMapper.factory.createParser(nameSpacesBytes) as CBORParser

        if (parser.nextToken() != JsonToken.START_OBJECT) return emptyMap()

        while (parser.nextToken() == JsonToken.FIELD_NAME) {
            val nameSpace = parser.currentName()
            val items = mutableListOf<ByteArray>()

            if (parser.nextToken() != JsonToken.START_ARRAY) break

            while (parser.nextToken() != JsonToken.END_ARRAY) {
                items.add(parser.binaryValue)
            }
            result[nameSpace] = items
        }
        parser.close()
        return result
    }

    /**
     * Filters items against requested elements, handling age_over_NN nearest-match logic.
     */
    private fun filterItems(
        itemBytes: List<ByteArray>,
        requestedElements: Map<String, Boolean>
    ): List<MatchedItem> {
        val exactRequested = requestedElements.keys.filter { !isAgeOverNN(it) }.toSet()
        val ageOverRequests = requestedElements.keys.filter { isAgeOverNN(it) }

        val matchedItems = mutableListOf<MatchedItem>()

        val decodedItems = itemBytes.mapNotNull { bytes ->
            val identifier = readElementIdentifier(bytes) ?: return@mapNotNull null
            identifier to bytes
        }

        for ((identifier, bytes) in decodedItems) {
            if (identifier in exactRequested) {
                matchedItems.add(
                    MatchedItem(
                        identifier = identifier,
                        intentToRetain = requestedElements[identifier] ?: false,
                        bytes = bytes
                    )
                )
            }
        }

        val ageOverItems = decodedItems.filter { (id, _) -> isAgeOverNN(id) }
        for (requestedKey in ageOverRequests) {
            val requestedAge = parseAgeOverNN(requestedKey) ?: continue
            val intentToRetain = requestedElements[requestedKey] ?: false
            resolveAgeOver(requestedAge, intentToRetain, ageOverItems)?.let { matchedItems.add(it) }
        }

        return matchedItems
    }

    private fun readElementIdentifier(itemBytes: ByteArray): String? = try {
        (cborMapper.readTree(itemBytes) as? ObjectNode)?.get(KEY_ELEMENT_IDENTIFIER)?.asText()
    } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
        logger.debug(logTag, "Failed to read elementIdentifier: ${e.message}")
        null
    }

    private fun readBooleanValue(itemBytes: ByteArray): Boolean? = try {
        (cborMapper.readTree(itemBytes) as? ObjectNode)?.get(KEY_ELEMENT_VALUE)?.asBoolean()
    } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
        logger.debug(logTag, "Failed to read elementValue: ${e.message}")
        null
    }

    /**
     * Nearest-match logic:
     * 1. Closest TRUE where stored age >= requestedAge (prefer lowest)
     * 2. Closest FALSE where stored age <= requestedAge (prefer highest)
     * 3. null if neither found
     */
    private fun resolveAgeOver(
        requestedAge: Int,
        intentToRetain: Boolean,
        ageOverItems: List<Pair<String, ByteArray>>
    ): MatchedItem? {
        data class AgeItem(
            val identifier: String,
            val age: Int,
            val value: Boolean,
            val bytes: ByteArray
        )

        val parsed = ageOverItems.mapNotNull { (identifier, bytes) ->
            val age = parseAgeOverNN(identifier) ?: return@mapNotNull null
            val value = readBooleanValue(bytes) ?: return@mapNotNull null
            AgeItem(identifier, age, value, bytes)
        }

        val resolved = parsed.filter { it.value && it.age >= requestedAge }.minByOrNull { it.age }
            ?: parsed.filter { !it.value && it.age <= requestedAge }.maxByOrNull { it.age }

        return resolved?.let { MatchedItem(it.identifier, intentToRetain, it.bytes) }
    }

    private fun isAgeOverNN(identifier: String) = AGE_OVER_PATTERN.matches(identifier)

    private fun parseAgeOverNN(identifier: String): Int? =
        AGE_OVER_PATTERN.find(identifier)?.groupValues?.get(1)?.toIntOrNull()

    /**
     * A credential item that matched the request: the resolved element identifier, its
     * intent-to-retain flag and the original IssuerSignedItemBytes.
     */
    private data class MatchedItem(
        val identifier: String,
        val intentToRetain: Boolean,
        val bytes: ByteArray
    )

    private companion object {
        val AGE_OVER_PATTERN = Regex("^age_over_(\\d{2})$")
        const val KEY_ELEMENT_IDENTIFIER = "elementIdentifier"
        const val KEY_ELEMENT_VALUE = "elementValue"
        const val LOG_NO_MATCHING_NAMESPACES =
            "SessionData termination initiated due to no matching NameSpaces"
        const val LOG_NO_MATCHING_ATTRIBUTES =
            "SessionData termination initiated due to no matching attributes"
        const val LOG_AGE_OVER_LIMIT =
            "SessionData termination initiated due to exceeding age_over_NN request limit"
    }
}
