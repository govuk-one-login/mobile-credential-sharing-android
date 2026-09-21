package uk.gov.onelogin.sharing.cryptoService.cbor.decoders

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.dataformat.cbor.CBORParser
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMapper
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DocRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequestDto

@ContributesBinding(AppScope::class)
class DeviceRequestDecoderImpl(val logger: Logger) : DeviceRequestDecoder {

    override fun deviceRequestDecoder(bytes: ByteArray): DeviceRequest = try {
        parseDeviceRequest(bytes)
    } catch (e: DeviceRequestValidationException) {
        throw e
    } catch (e: DeviceRequestDecodingException) {
        throw e
    } catch (e: JsonProcessingException) {
        logger.error(logger.logTag, "DeviceRequest CBOR decoding failed: ${e.message}")
        throw DeviceRequestDecodingException(e.message ?: "CBOR decoding error", e)
    } catch (e: IllegalArgumentException) {
        logger.error(logger.logTag, "DeviceRequest CBOR decoding failed: ${e.message}")
        throw DeviceRequestDecodingException(e.message ?: "CBOR decoding error", e)
    }

    private fun parseDeviceRequest(bytes: ByteArray): DeviceRequest {
        val parser = CborMapper.default.factory.createParser(bytes) as CBORParser
        return parser.use { p ->
            validateRootMap(p)
            var version: String? = null
            val docRequests = mutableListOf<DocRequest>()
            val topLevelKeys = mutableSetOf<String>()

            while (p.nextToken() != JsonToken.END_OBJECT) {
                val key = p.currentName()
                checkDuplicateKey(topLevelKeys, key, "DeviceRequest")
                p.nextToken()

                when (key) {
                    VERSION_KEY -> version = p.text
                    DOC_REQUESTS_KEY -> docRequests.addAll(parseDocRequestsArray(p, bytes))
                    else -> p.skipChildren()
                }
            }

            validateTrailingData(p, bytes.size)
            validateDeviceRequest(version, docRequests)

            logger.debug(logger.logTag, "device request decoded successfully")
            DeviceRequest(version = version!!, docRequests = docRequests)
        }
    }

    private fun validateRootMap(p: CBORParser) {
        if (p.nextToken() != JsonToken.START_OBJECT) {
            val errorMessage = "DeviceRequest CBOR decoding failed: Root must be a map"
            logger.error(logger.logTag, errorMessage)
            throw DeviceRequestDecodingException(errorMessage)
        }
    }

    private fun parseDocRequestsArray(p: CBORParser, bytes: ByteArray): List<DocRequest> {
        if (p.currentToken != JsonToken.START_ARRAY) {
            val errorMessage = "DeviceRequest CBOR decoding failed: docRequests must be an array"
            logger.error(logger.logTag, errorMessage)
            throw DeviceRequestDecodingException(errorMessage)
        }
        val list = mutableListOf<DocRequest>()
        while (p.nextToken() != JsonToken.END_ARRAY) {
            if (p.currentToken != JsonToken.START_OBJECT) {
                val errorMessage =
                    "DeviceRequest CBOR decoding failed: DocRequest item must be a map"
                logger.error(logger.logTag, errorMessage)
                throw DeviceRequestDecodingException(errorMessage)
            }
            list.add(parseDocRequest(p, bytes))
        }
        return list
    }

    private fun validateTrailingData(p: CBORParser, sourceSize: Int) {
        val endOffset = p.currentLocation().byteOffset.toInt()
        if (endOffset < sourceSize) {
            val errorMessage =
                "DeviceRequest CBOR decoding failed: Trailing data found in DeviceRequest CBOR"
            logger.error(logger.logTag, errorMessage)
            throw DeviceRequestDecodingException(errorMessage)
        }
    }

    private fun validateDeviceRequest(version: String?, docRequests: List<DocRequest>) {
        if (version.isNullOrEmpty()) {
            val errorMessage =
                "DeviceRequest CBOR decoding failed: Missing or empty version in DeviceRequest"
            logger.error(logger.logTag, errorMessage)
            throw DeviceRequestDecodingException(errorMessage)
        }
        if (docRequests.isEmpty()) {
            val errorMessage = "DeviceRequest contains empty DocRequests"
            logger.error(logger.logTag, errorMessage)
            throw DeviceRequestValidationException(errorMessage)
        }
    }

    private fun parseDocRequest(parser: CBORParser, source: ByteArray): DocRequest {
        var itemsRequest: ItemsRequest? = null
        var itemsRequestBytes: ByteArray? = null
        var rawReaderAuth: ByteArray? = null
        val docKeys = mutableSetOf<String>()

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            val key = parser.currentName()
            checkDuplicateKey(docKeys, key, "DocRequest")
            parser.nextToken()

            when (key) {
                ITEMS_REQUEST_KEY -> {
                    val (req, reqBytes) = parseItemsRequestField(parser, source)
                    itemsRequest = req
                    itemsRequestBytes = reqBytes
                }

                READER_AUTH_KEY -> rawReaderAuth = parseReaderAuthField(parser, source)

                else -> parser.skipChildren()
            }
        }

        if (itemsRequest == null || itemsRequestBytes == null) {
            val errorMessage =
                "DeviceRequest CBOR decoding failed: Missing or invalid itemsRequest in DocRequest"
            logger.error(logger.logTag, errorMessage)
            throw DeviceRequestDecodingException(errorMessage)
        }

        return DocRequest(
            itemsRequest = itemsRequest,
            readerAuth = rawReaderAuth,
            itemsRequestBytes = itemsRequestBytes
        )
    }

    private fun parseItemsRequestField(
        parser: CBORParser,
        source: ByteArray
    ): Pair<ItemsRequest, ByteArray> {
        val startOffset = parser.currentTokenLocation().byteOffset.toInt()
        val payload = if (parser.currentToken == JsonToken.VALUE_EMBEDDED_OBJECT ||
            parser.currentToken == JsonToken.VALUE_STRING
        ) {
            parser.binaryValue
        } else {
            null
        }
        val endOffset = parser.currentLocation().byteOffset.toInt()

        if (payload == null || startOffset !in 0..endOffset || endOffset > source.size) {
            val errorMessage =
                "DeviceRequest CBOR decoding failed: itemsRequest must be Tag 24 wrapped bytes"
            logger.error(logger.logTag, errorMessage)
            throw DeviceRequestDecodingException(errorMessage)
        }

        val itemsBytes = source.copyOfRange(startOffset, endOffset)
        val dto = CborMapper.default.readValue(payload, ItemsRequestDto::class.java)
        return Pair(ItemsRequest(dto.docType, dto.nameSpaces), itemsBytes)
    }

    private fun parseReaderAuthField(parser: CBORParser, source: ByteArray): ByteArray {
        val startOffset = parser.currentTokenLocation().byteOffset.toInt()
        parser.skipChildren()
        val endOffset = parser.currentLocation().byteOffset.toInt()
        return source.copyOfRange(startOffset, endOffset)
    }

    private fun checkDuplicateKey(seenKeys: MutableSet<String>, key: String, contextName: String) {
        if (!seenKeys.add(key)) {
            val errorMessage =
                "DeviceRequest CBOR decoding failed: Duplicate key in $contextName: $key"
            logger.error(logger.logTag, errorMessage)
            throw DeviceRequestDecodingException(errorMessage)
        }
    }

    private companion object {
        private const val VERSION_KEY = "version"
        private const val DOC_REQUESTS_KEY = "docRequests"
        private const val ITEMS_REQUEST_KEY = "itemsRequest"
        private const val READER_AUTH_KEY = "readerAuth"
    }
}
