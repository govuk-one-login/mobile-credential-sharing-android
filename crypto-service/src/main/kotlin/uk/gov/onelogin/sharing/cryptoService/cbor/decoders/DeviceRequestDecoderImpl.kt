package uk.gov.onelogin.sharing.cryptoService.cbor.decoders

import com.fasterxml.jackson.core.JsonProcessingException
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.cryptoService.cbor.CborMapper
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.devicerequest.DeviceRequestDto
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DocRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequest

@ContributesBinding(AppScope::class)
class DeviceRequestDecoderImpl(val logger: Logger) : DeviceRequestDecoder {
    override fun deviceRequestDecoder(bytes: ByteArray): DeviceRequest = try {
        val deviceRequestDto = CborMapper.default.readValue(
            bytes,
            DeviceRequestDto::class.java
        )

        if (deviceRequestDto.docRequest.isEmpty()) {
            val errorMessage = "empty DocRequest: status code 20"
            logger.error(logger.logTag, errorMessage)
            throw DeviceRequestDecodingException(errorMessage)
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
    } catch (e: DeviceRequestDecodingException) {
        throw e
    } catch (e: JsonProcessingException) {
        logger.error(logger.logTag, "session termination: status code 11")
        throw DeviceRequestDecodingException(e.message ?: "CBOR decoding error", e)
    } catch (e: IllegalArgumentException) {
        logger.error(logger.logTag, "session termination: status code 11")
        throw DeviceRequestDecodingException(e.message ?: "CBOR decoding error", e)
    }
}
