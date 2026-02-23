package uk.gov.onelogin.sharing.security.cbor

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.exc.StreamReadException
import com.fasterxml.jackson.databind.DatabindException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.IOException
import java.util.Base64
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.core.implementation.ImplementationDetail
import uk.gov.onelogin.sharing.core.implementation.RequiresImplementation
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.security.cbor.CborErrors.DECODING_ERROR
import uk.gov.onelogin.sharing.security.cbor.decoders.DeriveUntaggedCborImpl
import uk.gov.onelogin.sharing.security.cbor.decoders.SessionTranscriptDecoderImpl
import uk.gov.onelogin.sharing.security.cbor.dto.DeviceEngagementDto
import uk.gov.onelogin.sharing.security.cbor.dto.SessionEstablishmentDto
import uk.gov.onelogin.sharing.security.cbor.serializers.EmbeddedCbor

private const val TAG = "decodeDeviceEngagement"

/**
 * Decodes a CBOR-encoded, Base64 URL-safe string into a [DeviceEngagementDto] object.
 *
 * The Base64 URL string is decoded into a raw CBOR byte array.
 *
 * The Jackson ObjectMapper is then used to deserialize the byte array into [DeviceEngagementDto]
 *
 * Successful deserialization will print data to the console. If there are any exceptions, the stack
 * trace will be printed.
 *
 * @param cborBase64Url The CBOR-encoded data represented as a Base64 URL string.
 * @param logger An instance of [Logger] for logging events.
 */
fun decodeDeviceEngagement(cborBase64Url: String, logger: Logger): DeviceEngagementDto? = try {
    val cborData = cborBase64Url.base64Decode()

    val cborMapper = ObjectMapper(CBORFactory()).apply {
        registerModule(KotlinModule.Builder().build())
    }

    val deviceEngagement: DeviceEngagementDto = cborMapper.readValue(cborData)
    logger.debug(TAG, "Successfully deserialized DeviceEngagementDto:")
    @RequiresImplementation(
        details = [
            ImplementationDetail(
                ticket = "N/A not captured",
                description = "Create DTO -> Domain mapping functions for verifier to extract" +
                    "deserialized device engagement message"
            )
        ]
    )
    logger.debug(TAG, " - Version: ${deviceEngagement.version}")
    logger.debug(
        TAG,
        " - Security - Cipher Suite: " +
            "${deviceEngagement.security.cipherSuiteIdentifier}"
    )
    logger.debug(
        TAG,
        " - Security - Ephemeral Public Key (as hex): " +
            "${deviceEngagement.security.ephemeralPublicKey}"
    )
    logger.debug(
        TAG,
        " - Device Retrieval Methods: " +
            "${deviceEngagement.deviceRetrievalMethods}"
    )

    deviceEngagement
} catch (e: JsonProcessingException) {
    // We need to send error status code 10 to the reader in the event of CBOR decoding errors
    logger.debug(TAG, "Failed to deserialize CBOR: ${e.message}")
    null
} catch (e: IllegalArgumentException) {
    logger.debug(TAG, "Illegal parameters found: ${e.message}")
    null
}

@Throws(
    IllegalArgumentException::class
)
fun String.base64Decode(decoder: Base64.Decoder = Base64.getUrlDecoder()): ByteArray =
    decoder.decode(this)

@Throws(
    IllegalArgumentException::class,
    IOException::class,
    StreamReadException::class,
    DatabindException::class
)
fun decodeSessionEstablishmentModel(rawBytes: ByteArray, logger: Logger): SessionEstablishmentDto =
    try {
        val mapper = ObjectMapper(CBORFactory()).apply {
            registerModule(KotlinModule.Builder().build())
        }

        val rawDto = mapper.readValue(rawBytes, SessionEstablishmentDto::class.java)
        requireNotNull(rawDto) {
            DECODING_ERROR.errorMessage
        }

        val sessionEstablishmentDto = SessionEstablishmentDto(
            eReaderKey = EmbeddedCbor(rawDto.eReaderKey.encodeCbor()),
            data = rawDto.data
        )

        logger.debug(
            logger.logTag,
            "eReaderKey: ${sessionEstablishmentDto.eReaderKey.encoded.toHexString()}, " +
                "data: ${sessionEstablishmentDto.data.toHexString()} "
        )

        sessionEstablishmentDto
    } catch (e: IllegalArgumentException) {
        logger.debug(logger.logTag, e.message.toString())
        throw e
    }

@Throws(
    IllegalArgumentException::class,
    IOException::class,
    StreamReadException::class,
    DatabindException::class
)
fun deriveSessionTranscript(
    cborBase64Url: String,
    sessionEstablishmentBytes: ByteArray,
    logger: Logger
): ByteArray = SessionTranscriptDecoderImpl(logger).deriveSessionTranscript(
    cborBase64Url = cborBase64Url,
    sessionEstablishmentBytes = sessionEstablishmentBytes
)

private fun encodeTag24Bstr(payload: ByteArray): ByteArray {
    val out = java.io.ByteArrayOutputStream()
    out.write(0xD8) // tag(24)
    out.write(0x18)
    val n = payload.size
    when {
        n < 24 -> out.write(0x40 + n)
        n <= 0xFF -> { out.write(0x58); out.write(n) }
        n <= 0xFFFF -> { out.write(0x59); out.write((n ushr 8) and 0xFF); out.write(n and 0xFF) }
        else -> { out.write(0x5A); out.write((n ushr 24) and 0xFF); out.write((n ushr 16) and 0xFF); out.write((n ushr 8) and 0xFF); out.write(n and 0xFF) }
    }
    out.write(payload)
    return out.toByteArray()
}

fun deriveSessionTranscript(
    deviceEngagementCborBytes: ByteArray,
    eReaderKeyTagged: ByteArray,
): ByteArray {
    val taggedDevEng = encodeTag24Bstr(deviceEngagementCborBytes)

    val out = java.io.ByteArrayOutputStream()
    out.write(0x83)              // array(3)
    out.write(taggedDevEng)      // element #1
    out.write(eReaderKeyTagged)  // element #2 (already tag24(bstr(...)))
    out.write(0xF6)              // element #3 null
    return out.toByteArray()
}

fun deriveUntaggedCbor(tagged: ByteArray): ByteArray =
    DeriveUntaggedCborImpl().deriveUntaggedCbor(tagged)

fun prettyPrintDecryptedCbor(bytes: ByteArray, logger: Logger) {
    val mapper = ObjectMapper(CBORFactory())
    val node = mapper.readTree(bytes)
    logger.debug("Decrypted", node.toPrettyString())
}