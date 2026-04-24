package uk.gov.onelogin.sharing.cryptoService.cbor

import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator
import java.io.ByteArrayOutputStream
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.DeviceResponseDto
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.SessionEstablishmentDto
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.BleOptionsSerializer
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.CoseKeySerializer
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.DeviceEngagementSerializer
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.DeviceRetrievalMethodSerializer
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.EmbeddedCbor
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.EmbeddedCborSerializer
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.SecuritySerializer
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.SessionEstablishmentSerializer
import uk.gov.onelogin.sharing.cryptoService.cose.CoseKey
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleOptions
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.DeviceRetrievalMethod
import uk.gov.onelogin.sharing.models.mdoc.engagment.DeviceEngagement
import uk.gov.onelogin.sharing.models.mdoc.security.Security
import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionData
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequest

/**
 * A private generic function that takes ('Any') map of custom serializers to encode using
 * [CborMapper.create] and returns a [ByteArray] object
 *
 * @param serializers A map of classes to the custom serializers they require for CBOR encoding.
 * @return A [ByteArray] containing the CBOR representation of the object.
 */
private fun Any.encodeCbor(serializers: Map<Class<*>, StdSerializer<*>>): ByteArray {
    val mapper = CborMapper.create(serializers)
    return mapper.writeValueAsBytes(this)
}

/**
 * Encodes a [CoseKey] object into a CBOR byte array.
 *
 * @receiver the [CoseKey] object to be encoded.
 * @return A [ByteArray] containing the CBOR representation of the [CoseKey]
 */
fun CoseKey.encodeCbor(): ByteArray {
    val coseKeySerializers: Map<Class<*>, StdSerializer<*>> = mapOf(
        CoseKey::class.java to CoseKeySerializer()
    )
    return this.encodeCbor(coseKeySerializers)
}

/**
 * Encodes [DeviceEngagement] object into a CBOR byte array.
 *
 * Takes a map of all required custom serializers to form the CBOR object.
 *
 *  * @receiver the [DeviceEngagement] object to be encoded.
 *  * @return A [ByteArray] containing the CBOR representation of the [DeviceEngagement]
 */
fun DeviceEngagement.encodeCbor(): ByteArray {
    val deviceEngagementSerializers: Map<Class<*>, StdSerializer<*>> = mapOf(
        DeviceEngagement::class.java to DeviceEngagementSerializer(),
        DeviceRetrievalMethod::class.java to DeviceRetrievalMethodSerializer(),
        BleOptions::class.java to BleOptionsSerializer(),
        Security::class.java to SecuritySerializer(),
        EmbeddedCbor::class.java to EmbeddedCborSerializer(),
        CoseKey::class.java to CoseKeySerializer()
    )
    return this.encodeCbor(deviceEngagementSerializers)
}

fun Any.encodeCbor(): ByteArray {
    val sessionSerializers: Map<Class<*>, StdSerializer<*>> = mapOf(
        EmbeddedCbor::class.java to EmbeddedCborSerializer(),
        SessionEstablishmentDto::class.java to SessionEstablishmentSerializer()
    )
    return this.encodeCbor(sessionSerializers)
}

/**
 * Extension to encode the [DeviceResponseDto.DeviceResponse] to CBOR bytes.
 */
fun DeviceResponseDto.DeviceResponse.encodeCbor(): ByteArray {
    val serializers: Map<Class<*>, StdSerializer<*>> = mapOf(
        EmbeddedCbor::class.java to (EmbeddedCborSerializer() as StdSerializer<*>)
    )
    val mapper = CborMapper.create(serializers)
    return mapper.writeValueAsBytes(this)
}

/**
 * Encodes the [ItemsRequest] fields into a raw CBOR byte array without Tag 24 wrapping.
 *
 * Used by [DeviceRequest.encodeCbor], which writes Tag 24 directly via
 * [CBORGenerator.writeTag] to avoid double-wrapping.
 *
 * @receiver The [ItemsRequest] to encode.
 * @return A [ByteArray] containing the raw CBOR representation of the [ItemsRequest].
 */
fun ItemsRequest.encodeCbor(): ByteArray = ByteArrayOutputStream().also { output ->
    CBORFactory().createGenerator(output).use { gen ->
        gen.writeStartObject(2)
        gen.writeStringField("docType", docType)
        gen.writeFieldName("nameSpaces")
        gen.writeStartObject(nameSpaces.size)
        nameSpaces.forEach { (namespace, elements) ->
            gen.writeFieldName(namespace)
            gen.writeStartObject(elements.size)
            elements.forEach { (identifier, intentToRetain) ->
                gen.writeBooleanField(identifier, intentToRetain)
            }
            gen.writeEndObject()
        }
        gen.writeEndObject()
        gen.writeEndObject()
    }
}.toByteArray()

/**
 * Encodes a [SessionData] into a CBOR map as defined by ISO 18013-5.
 *
 * Null fields are omitted entirely from the resulting map.
 *
 * @receiver The [SessionData] to encode.
 * @return A [ByteArray] containing the CBOR representation.
 */
fun SessionData.encodeCbor(): ByteArray {
    val output = ByteArrayOutputStream()
    val fieldCount = listOfNotNull(data, status).size
    CBORFactory().createGenerator(output).use { gen ->
        gen.writeStartObject(fieldCount)
        data?.let { gen.writeBinaryField("data", it) }
        status?.let { gen.writeNumberField("status", it.code.toLong()) }
        gen.writeEndObject()
    }
    return output.toByteArray()
}

/**
 * Encodes a [DeviceRequest] into a raw CBOR byte array as defined by ISO 18013-5.
 *
 * The top-level structure is a CBOR map with `version` and `docRequests`.
 * Each [DocRequest] contains an `itemsRequest` field encoded as Tag 24-wrapped bytes.
 * The [DeviceRequest] itself is not wrapped in Tag 24.
 *
 * @receiver The [DeviceRequest] to encode.
 * @return A [ByteArray] containing the raw CBOR representation.
 */
fun DeviceRequest.encodeCbor(): ByteArray {
    val output = ByteArrayOutputStream()
    CBORFactory().createGenerator(output).use { gen ->
        gen.writeStartObject(2)
        gen.writeStringField("version", version)
        gen.writeFieldName("docRequests")
        gen.writeStartArray(docRequests, docRequests.size)
        docRequests.forEach { docRequest ->
            gen.writeStartObject(1)
            gen.writeFieldName("itemsRequest")
            (gen as CBORGenerator).writeTag(EmbeddedCborSerializer.EMBEDDED_CBOR_TAG)
            gen.writeBinary(docRequest.itemsRequest.encodeCbor())
            gen.writeEndObject()
        }
        gen.writeEndArray()
        gen.writeEndObject()
    }
    return output.toByteArray()
}
