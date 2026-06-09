package uk.gov.onelogin.sharing.cryptoService.cbor.decoders

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMapper
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponse
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponseDto

@ContributesBinding(AppScope::class)
class DeviceResponseDecoderImpl(private val logger: Logger) : DeviceResponseDecoder {

    override fun decode(bytes: ByteArray): DeviceResponse = try {
        val dto = CborMapper.default.readValue(
            bytes,
            DeviceResponseDto.DeviceResponseDTO::class.java
        )

        logger.debug(logTag, "DeviceResponse decoded successfully")

        dto.toDomain()
    } catch (e: IllegalArgumentException) {
        logger.error(logTag, LOG_CBOR_VALIDATION_ERROR, e)
        throw DeviceResponseDecodingException(e.message ?: LOG_CBOR_VALIDATION_ERROR, e)
    } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
        logger.error(logTag, LOG_CBOR_DECODING_ERROR, e)
        throw DeviceResponseDecodingException(LOG_CBOR_DECODING_ERROR, e)
    }

    companion object {
        const val LOG_CBOR_DECODING_ERROR = "CBOR decoding error: invalid DeviceResponse structure"
        const val LOG_CBOR_VALIDATION_ERROR = "CBOR validation error"
    }
}
