package uk.gov.onelogin.sharing.cryptoService.cbor

import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator
import java.io.ByteArrayOutputStream
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.devicerequest.toDto
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.BleOptionsSerializer
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.toDto
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.DeviceEngagementSerializer
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.DeviceRetrievalMethodSerializer
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.EmbeddedCbor
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.EmbeddedCborSerializer
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.SecuritySerializer
import uk.gov.onelogin.sharing.cryptoService.cose.CoseKey
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleOptions
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.DeviceRetrievalMethod
import uk.gov.onelogin.sharing.models.mdoc.engagment.DeviceEngagement
import uk.gov.onelogin.sharing.models.mdoc.security.Security
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceAuthentication

private const val CBOR_ARRAY_4 = 0x84

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
fun CoseKey.encodeCbor(): ByteArray = toDto().toCbor()

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
        EmbeddedCbor::class.java to EmbeddedCborSerializer()
    )
    return this.encodeCbor(deviceEngagementSerializers)
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
fun ItemsRequest.encodeCbor(): ByteArray = toDto().toCbor()

/**
 * Encodes a [DeviceAuthentication] into DeviceAuthenticationBytes
 *
 * The [DeviceAuthentication] is serialised as a 4-element CBOR array
 * `["DeviceAuthentication", SessionTranscript, DocType, DeviceNameSpacesBytes]`
 *
 * @receiver The [DeviceAuthentication] to encode.
 * @return A [ByteArray] containing the Tag-24-wrapped CBOR representation.
 */
fun DeviceAuthentication.encodeCbor(): ByteArray {
    val deviceAuthenticationArray = ByteArrayOutputStream().also { out ->
        // CBOR definite-length array header for 4 elements
        out.write(CBOR_ARRAY_4)
        CBORFactory().createGenerator(out).use { gen -> gen.writeString(label) }
        out.write(sessionTranscript)
        CBORFactory().createGenerator(out).use { gen -> gen.writeString(docType) }
        out.write(deviceNameSpacesBytes)
    }.toByteArray()
    return EmbeddedCbor(deviceAuthenticationArray).toCbor()
}

/**
 * Encodes an empty CBOR map wrapped in Tag 24.
 *
 * Note: we are not sending any device-signed namespaces for the MVP.
 *
 * @return A [ByteArray] containing the Tag-24-wrapped empty CBOR map.
 */
fun encodeDeviceNameSpacesBytes(): ByteArray {
    val emptyMap = ByteArrayOutputStream().also { out ->
        CBORFactory().createGenerator(out).use { gen ->
            gen.writeStartObject(0)
            gen.writeEndObject()
        }
    }.toByteArray()
    return EmbeddedCbor(emptyMap).toCbor()
}

/**
 * Encodes a [DeviceRequest] into a raw CBOR byte array as defined by ISO 18013-5.
 *
 * @receiver The [DeviceRequest] to encode.
 * @return A [ByteArray] containing the raw CBOR representation.
 */
fun DeviceRequest.encodeCbor(): ByteArray = toDto().toCbor()
