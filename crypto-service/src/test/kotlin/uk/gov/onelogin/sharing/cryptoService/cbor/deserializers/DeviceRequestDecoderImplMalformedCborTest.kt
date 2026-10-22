package uk.gov.onelogin.sharing.cryptoService.cbor.deserializers

import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.Matchers
import kotlin.test.assertFailsWith
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import uk.gov.logging.api.v3.MemorisedLogger
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.DeviceRequestDecoderImpl
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.DeviceRequestDecodingException
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.cbor
import uk.gov.onelogin.sharing.cryptoService.util.assertErrorLoggedWithThrowable
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMapper
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequestDto

@RunWith(Parameterized::class)
class DeviceRequestDecoderImplMalformedCborTest(
    private val caseName: String,
    private val input: ByteArray,
    private val expectedMessage: String
) {
    private val logger = MemorisedLogger()
    private val deviceRequestDecoderImpl = DeviceRequestDecoderImpl(logger)

    @Test
    fun `malformed cbor throws and logs decoding exception`() {
        assertFailsWith<DeviceRequestDecodingException>("case: $caseName") {
            deviceRequestDecoderImpl.deviceRequestDecoder(input)
        }

        logger.assertErrorLoggedWithThrowable(
            DeviceRequestDecodingException::class.java,
            containsString(expectedMessage)
        )
    }

    companion object {
        private const val DOC_TYPE = "org.iso.18013.5.1.mDL"
        private const val NAME_SPACE = "org.iso.18013.5.1"

        @JvmStatic
        @Parameters(name = "{0}")
        @Suppress("LongMethod")
        fun cases(): List<Array<Any>> = listOf(
            arrayOf(
                "docRequests is not an array",
                cbor { gen ->
                    gen.writeStartObject(2)
                    gen.writeFieldName("version")
                    gen.writeString("1.0")
                    gen.writeFieldName("docRequests")
                    gen.writeString("notAnArray")
                    gen.writeEndObject()
                },
                "docRequests must be an array"
            ),
            arrayOf(
                "docRequests array element is not a map",
                cbor { gen ->
                    gen.writeStartObject(2)
                    gen.writeFieldName("version")
                    gen.writeString("1.0")
                    gen.writeFieldName("docRequests")
                    gen.writeStartArray(null, 1)
                    gen.writeString("notAMap")
                    gen.writeEndArray()
                    gen.writeEndObject()
                },
                "DocRequest item must be a map"
            ),
            arrayOf(
                "docRequest without itemsRequest",
                cbor { gen ->
                    gen.writeStartObject(2)
                    gen.writeFieldName("version")
                    gen.writeString("1.0")
                    gen.writeFieldName("docRequests")
                    gen.writeStartArray(null, 1)
                    gen.writeStartObject(0)
                    gen.writeEndObject()
                    gen.writeEndArray()
                    gen.writeEndObject()
                },
                "Missing or invalid itemsRequest"
            ),
            arrayOf(
                "itemsRequest is not tag 24 wrapped bytes",
                cbor { gen ->
                    gen.writeStartObject(2)
                    gen.writeFieldName("version")
                    gen.writeString("1.0")
                    gen.writeFieldName("docRequests")
                    gen.writeStartArray(null, 1)
                    gen.writeStartObject(1)
                    gen.writeFieldName("itemsRequest")
                    gen.writeNumber(42)
                    gen.writeEndObject()
                    gen.writeEndArray()
                    gen.writeEndObject()
                },
                "itemsRequest must be Tag 24 wrapped bytes"
            ),
            arrayOf(
                "missing version with valid docRequests",
                cbor { gen ->
                    val itemsRequestBytes = CborMapper.default.writeValueAsBytes(
                        ItemsRequestDto(
                            docType = DOC_TYPE,
                            nameSpaces = mapOf(NAME_SPACE to mapOf("family_name" to true))
                        )
                    )
                    gen.writeStartObject(1)
                    gen.writeFieldName("docRequests")
                    gen.writeStartArray(null, 1)
                    gen.writeStartObject(1)
                    gen.writeFieldName("itemsRequest")
                    gen.writeBinary(itemsRequestBytes)
                    gen.writeEndObject()
                    gen.writeEndArray()
                    gen.writeEndObject()
                },
                "Missing or empty version"
            )
        )
    }
}
