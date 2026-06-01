package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import uk.gov.onelogin.sharing.verification.format.document.IssuerSigned

class SharingIssuerSignedSerializer : KSerializer<IssuerSigned> {
    override val descriptor: SerialDescriptor
        get() = SharingDeviceSigned.serializer().descriptor

    override fun serialize(encoder: Encoder, value: IssuerSigned) = encoder.encodeSerializableValue(
        SharingIssuerSigned.serializer(),
        value as SharingIssuerSigned
    )

    override fun deserialize(decoder: Decoder): SharingIssuerSigned =
        decoder.decodeSerializableValue(SharingIssuerSigned.serializer())
}
