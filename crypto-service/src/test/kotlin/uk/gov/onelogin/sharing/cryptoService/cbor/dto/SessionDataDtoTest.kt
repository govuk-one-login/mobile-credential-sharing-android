package uk.gov.onelogin.sharing.cryptoService.cbor.dto

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORConstants
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.startsWith
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasProperty
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.nullValue
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.cryptoService.cbor.HexFormatter
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.SessionDataDtoMatchers.hasData
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.SessionDataDtoMatchers.hasStatus
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.SessionDataDtoStubs.emptySessionDataDto
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.SessionDataDtoStubs.invalidStatusDto
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.SessionDataDtoStubs.validSessionDataHexString
import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionDataStatus

@RunWith(TestParameterInjector::class)
class SessionDataDtoTest {

    private val mapper: ObjectMapper = CBORMapper.builder()
        .build()

    private val dataFieldName = mapper.writeValueAsBytes("data").toHexString()
    private val statusFieldName = mapper.writeValueAsBytes("status").toHexString()

    @Test
    fun `Using a Mapper without CBOR features throw a JsonMappingException`() {
        val jsonMapper = ObjectMapper()

        val exception = assertFailsWith(JsonMappingException::class) {
            jsonMapper.writeValueAsBytes(emptySessionDataDto)
        }

        assertThat(
            exception,
            hasProperty<Throwable>(
                "cause",
                allOf(
                    instanceOf<IllegalArgumentException>(IllegalArgumentException::class.java),
                    hasProperty(
                        "message",
                        equalTo(
                            "Attempted to serialize '${SessionDataDto::class.java.name}' " +
                                "without a CBORGenerator instance."
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Invalid status codes throw IllegalArgumentExceptions`() {
        val exception = assertFailsWith(IllegalArgumentException::class) {
            invalidStatusDto()
        }

        assertThat(
            exception.message,
            equalTo(
                "Received invalid session data status: ${UInt.MAX_VALUE}"
            )
        )
    }

    @Test
    fun `DTOs with null properties serialize to CBOR objects with 0 elements`() {
        val result = mapper.writeValueAsBytes(emptySessionDataDto)
        assertThat(
            result.toHexString(),
            equalTo(HexFormatter(CBORConstants.PREFIX_TYPE_OBJECT))
        )
    }

    @Test
    fun `DTOs with only data serialize to a CBOR object with 1 element`() {
        val data = byteArrayOf(0x1, 0x2)
        val result = mapper.writeValueAsBytes(
            SessionDataDto(
                data = data
            )
        )

        assertThat(
            result.toHexString(),
            allOf(
                startsWith(
                    HexFormatter(CBORConstants.PREFIX_TYPE_OBJECT + 1)
                ),
                containsString(getDataFieldCborString(data)),
                not(containsString(statusFieldName))
            )
        )
    }

    @Test
    fun `DTOs with only status serialize to a CBOR object with 1 element`(
        @TestParameter status: SessionDataStatus
    ) {
        val result = mapper.writeValueAsBytes(
            SessionDataDto(status = status.code)
        )

        assertThat(
            result.toHexString(),
            allOf(
                startsWith(
                    HexFormatter(CBORConstants.PREFIX_TYPE_OBJECT + 1)
                ),
                containsString(getStatusFieldCborString(status.code)),
                not(containsString(dataFieldName))
            )
        )
    }

    @Test
    fun `Fully formed DTOs serialize to a CBOR object with 2 elements`() {
        val data = byteArrayOf(0x1, 0x2)

        val result = mapper.writeValueAsBytes(
            SessionDataDto(
                data = data,
                status = SessionDataStatus.SESSION_TERMINATION.code
            )
        )

        assertThat(
            result.toHexString(),
            allOf(
                startsWith(
                    HexFormatter(CBORConstants.PREFIX_TYPE_OBJECT + 2)
                ),
                containsString(getDataFieldCborString(data)),
                containsString(
                    getStatusFieldCborString(
                        SessionDataStatus.SESSION_TERMINATION.code
                    )
                )
            )
        )
    }

    //    @TestParameters(valuesProvider = ExampleSessionDataDtos::class)
    @Test
    fun `Can deserialize a previously serialized DTO`() {
        val sessionDataBytes = mapper.writeValueAsBytes(emptySessionDataDto)
        val result = mapper.readValue(sessionDataBytes, SessionDataDto::class.java)

        assertEquals(emptySessionDataDto, result)
    }

    @Test
    fun `Can deserialize a valid test data hex string`() {
        val result = mapper.readValue(validSessionDataHexString, SessionDataDto::class.java)
        assertThat(
            result,
            hasStatus(nullValue())
        )
        assertThat(
            result,
            hasData(not(nullValue()))
        )
    }

    private fun getDataFieldCborString(input: ByteArray) = dataFieldName +
        HexFormatter(CBORConstants.PREFIX_TYPE_BYTES + input.size) +
        input.toHexString()

    private fun getStatusFieldCborString(input: UInt) =
        statusFieldName + input.toUByte().toHexString()
}

// class ExampleSessionDataDtos : TestParametersValuesProvider() {
//    private val statusOnlyDtos = SessionDataStatus.entries.map { SessionDataDto(status = it.code) }
//    private val inputs = listOf(
//        "Empty SessionDataDto" to emptySessionDataDto,
//    ) + statusOnlyDtos
//
//    override fun provideValues(
//        context: Context?
//    ): List<TestParameters.TestParametersValues?>? = inputs.map { (name, dto) ->
//        TestParameters.TestParametersValues.builder()
//            .name(name)
//            .build()
//    }
// }
