package uk.gov.onelogin.sharing.security.cbor

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import java.util.Base64
import uk.gov.onelogin.sharing.core.implementation.ImplementationDetail
import uk.gov.onelogin.sharing.core.implementation.RequiresImplementation
import uk.gov.onelogin.sharing.security.cbor.dto.DeviceEngagementDto

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
 */
fun decodeDeviceEngagement(cborBase64Url: String): DeviceEngagementDto? {
    val cborData = cborBase64Url.base64Decode()

    val cborMapper = ObjectMapper(CBORFactory()).apply {
        registerModule(KotlinModule.Builder().build())
    }

    return try {
        val deviceEngagement: DeviceEngagementDto = cborMapper.readValue(cborData)
        println("Successfully deserialized DeviceEngagementDto:")
        @RequiresImplementation(
            details = [
                ImplementationDetail(
                    ticket = "N/A not captured",
                    description = "Create DTO -> Domain mapping functions for verifier to extract" +
                        "deserialized device engagement message"
                )
            ]
        )
        println(" - Version: ${deviceEngagement.version}")
        println(" - Security - Cipher Suite: ${deviceEngagement.security.cipherSuiteIdentifier}")
        println(
            " - Security - Ephemeral Public Key (as hex): ${deviceEngagement.security.ephemeralPublicKey}"
        )
        println(" - Device Retrieval Methods: ${deviceEngagement.deviceRetrievalMethods}")

        deviceEngagement
    } catch (e: JsonProcessingException) {
        // We need to send error status code 10 to the reader in the event of CBOR decoding errors
        println("Failed to deserialize CBOR: ${e.message}")
        null
    }
}

fun String.base64Decode(decoder: Base64.Decoder = Base64.getUrlDecoder()): ByteArray =
    decoder.decode(this)
