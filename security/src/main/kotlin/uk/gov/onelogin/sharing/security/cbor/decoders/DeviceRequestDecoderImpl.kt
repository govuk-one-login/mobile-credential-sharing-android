package uk.gov.onelogin.sharing.security.cbor.decoders

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelScope
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DocRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequest
import uk.gov.onelogin.sharing.security.cbor.dto.devicerequest.DeviceRequestDto
import uk.gov.onelogin.sharing.security.cbor.dto.devicerequest.DocRequestDto
import uk.gov.onelogin.sharing.security.cbor.serializers.EmbeddedCbor
import uk.gov.onelogin.sharing.security.cbor.serializers.EmbeddedCborSerializer

@ContributesBinding(ViewModelScope::class, binding = binding<DeviceRequestDecoder>())
class DeviceRequestDecoderImpl(val logger: Logger) : DeviceRequestDecoder {
    override fun deviceRequestDecoder(bytes: ByteArray): DeviceRequest = try {
        val cborMapper = ObjectMapper(
            CBORFactory()
        ).apply {
            registerKotlinModule()
            val module =
                SimpleModule().addSerializer(EmbeddedCbor::class.java, EmbeddedCborSerializer())
            registerModule(module)
        }

        val deviceRequestDto = cborMapper.readValue(
            bytes,
            DeviceRequestDto::class.java
        )

        if (deviceRequestDto.docRequest.isEmpty()) {
            val errorMessage = "empty DocRequest: status code 20"
            logger.error(logger.logTag, errorMessage)
            throw TypeNotPresentException(
                DocRequestDto::class.java.name,
                Exception(errorMessage)
            )
        }

        logger.debug(
            logger.logTag,
            "device request decoded successfully"
        )

        with(deviceRequestDto) {
            DeviceRequest(
                version = version,
                docRequests = docRequest.map {
                    DocRequest(
                        itemsRequest = ItemsRequest(
                            it.itemsRequest.docType,
                            it.itemsRequest.nameSpaces
                        )
                    )
                }
            )
        }
    } catch (e: MismatchedInputException) {
        logger.error(logger.logTag, "session termination: status code 11")
        throw e
    }
}
