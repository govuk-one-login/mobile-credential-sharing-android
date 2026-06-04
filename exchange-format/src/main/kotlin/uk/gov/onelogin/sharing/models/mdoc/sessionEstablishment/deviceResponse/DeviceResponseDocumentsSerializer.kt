package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class DeviceResponseDocumentsSerializer :
    KSerializer<List<SharingVerifiableDocumentWithPresentation>> {

    private val listSerializer = ListSerializer(
        SharingVerifiableDocumentWithPresentation.serializer()
    )
    override val descriptor: SerialDescriptor
        get() = listSerializer.descriptor

    override fun serialize(
        encoder: Encoder,
        value: List<SharingVerifiableDocumentWithPresentation>
    ) {
        encoder.encodeSerializableValue(listSerializer, value)
    }

    override fun deserialize(decoder: Decoder): List<SharingVerifiableDocumentWithPresentation> =
        decoder.decodeSerializableValue(listSerializer)
}
