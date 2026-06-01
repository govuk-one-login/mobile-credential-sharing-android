package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import uk.gov.onelogin.sharing.verification.format.document.device.DeviceSigned

class SharingDeviceSignedSerializer : KSerializer<DeviceSigned> {
    override val descriptor: SerialDescriptor
        get() = SharingDeviceSigned.serializer().descriptor

    override fun serialize(encoder: Encoder, value: DeviceSigned) = encoder.encodeSerializableValue(
        SharingDeviceSigned.serializer(),
        value as SharingDeviceSigned
    )

    override fun deserialize(decoder: Decoder): SharingDeviceSigned =
        decoder.decodeSerializableValue(SharingDeviceSigned.serializer())
}
