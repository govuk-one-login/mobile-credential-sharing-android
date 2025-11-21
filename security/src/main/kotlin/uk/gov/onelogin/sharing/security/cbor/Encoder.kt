package uk.gov.onelogin.sharing.security.cbor

import com.fasterxml.jackson.databind.ser.std.StdSerializer
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleOptions
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.DeviceRetrievalMethod
import uk.gov.onelogin.sharing.models.mdoc.engagment.DeviceEngagement
import uk.gov.onelogin.sharing.models.mdoc.security.Security
import uk.gov.onelogin.sharing.security.cbor.serializers.BleOptionsSerializer
import uk.gov.onelogin.sharing.security.cbor.serializers.CoseKeySerializer
import uk.gov.onelogin.sharing.security.cbor.serializers.DeviceEngagementSerializer
import uk.gov.onelogin.sharing.security.cbor.serializers.DeviceRetrievalMethodSerializer
import uk.gov.onelogin.sharing.security.cbor.serializers.EmbeddedCbor
import uk.gov.onelogin.sharing.security.cbor.serializers.EmbeddedCborSerializer
import uk.gov.onelogin.sharing.security.cbor.serializers.SecuritySerializer
import uk.gov.onelogin.sharing.security.cose.CoseKey

/**
 * A private generic function that takes ('Any') map of custom serializers to encode using
 * [CborMapper.create] and returns a [ByteArray] object
 *
 * @param serializers A map of classes to the custom serializers they require for CBOR encoding.
 * @return A [ByteArray] containing the CBOR representation of the object.
 */
private fun Any.encode(serializers: Map<Class<*>, StdSerializer<*>>): ByteArray {
    val mapper = CborMapper.create(serializers)
    return mapper.writeValueAsBytes(this)
}

/**
 * Encodes a [CoseKey] object into a CBOR byte array.
 *
 * @receiver the [CoseKey] object to be encoded.
 * @return A [ByteArray] containing the CBOR representation of the [CoseKey]
 */
fun CoseKey.encode(): ByteArray {
    val coseKeySerializers: Map<Class<*>, StdSerializer<*>> = mapOf(
        CoseKey::class.java to CoseKeySerializer()
    )
    return this.encode(coseKeySerializers)
}

/**
 * Encodes [DeviceEngagement] object into a CBOR byte array.
 *
 * Takes a map of all required custom serializers to form the CBOR object.
 *
 *  * @receiver the [DeviceEngagement] object to be encoded.
 *  * @return A [ByteArray] containing the CBOR representation of the [DeviceEngagement]
 */
fun DeviceEngagement.encode(): ByteArray {
    val deviceEngagementSerializers: Map<Class<*>, StdSerializer<*>> = mapOf(
        DeviceEngagement::class.java to DeviceEngagementSerializer(),
        DeviceRetrievalMethod::class.java to DeviceRetrievalMethodSerializer(),
        BleOptions::class.java to BleOptionsSerializer(),
        Security::class.java to SecuritySerializer(),
        EmbeddedCbor::class.java to EmbeddedCborSerializer(),
        CoseKey::class.java to CoseKeySerializer()
    )
    return this.encode(deviceEngagementSerializers)
}
