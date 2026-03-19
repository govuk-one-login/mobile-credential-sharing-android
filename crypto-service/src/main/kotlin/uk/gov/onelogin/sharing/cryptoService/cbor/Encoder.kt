package uk.gov.onelogin.sharing.cryptoService.cbor

import com.fasterxml.jackson.databind.ser.std.StdSerializer
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
