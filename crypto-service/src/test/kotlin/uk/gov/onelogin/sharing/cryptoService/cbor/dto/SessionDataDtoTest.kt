package uk.gov.onelogin.sharing.cryptoService.cbor.dto

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORConstants
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.google.testing.junit.testparameterinjector.TestParameters
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
import uk.gov.onelogin.sharing.models.mdoc.sessionData.ExampleSessionDataDtoInputs
import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionDataDto
import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionDataDtoMatchers.hasData
import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionDataDtoMatchers.hasStatus
import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionDataDtoStubs.dataFieldName
import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionDataDtoStubs.emptySessionDataDto
import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionDataDtoStubs.statusFieldName
import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionDataDtoStubs.validSessionDataDtoBytes
import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionDataStatus

@RunWith(TestParameterInjector::class)
class SessionDataDtoTest {

    private val mapper: ObjectMapper = CBORMapper.builder()
        .build()

    @Test
    fun `Using a Mapper without CBOR features throw a JsonMappingException`() {
        val jsonMapper = ObjectMapper()

        val exception = assertFailsWith(JsonMappingException::class) {
            emptySessionDataDto.toCbor(jsonMapper)
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

    /**
     * DCMAW-19837: AC1: Enforce SessionData status constraints
     */
    @Test
    fun `Invalid status codes throw IllegalArgumentExceptions`() {
        val exception = assertFailsWith(IllegalArgumentException::class) {
            SessionDataDto(status = UInt.MAX_VALUE)
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
        val result = emptySessionDataDto.toCbor(mapper)
        assertThat(
            result.toHexString(),
            equalTo(HexFormatter(CBORConstants.PREFIX_TYPE_OBJECT))
        )
    }

    /**
     * DCMAW-19837: AC1: Enforce SessionData status constraints
     */
    @Test
    fun `DTOs with only data serialize to a CBOR object with 1 element`() {
        val data = byteArrayOf(0x1, 0x2)
        val result = SessionDataDto(data = data).toCbor(mapper)

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

    /**
     * DCMAW-19837: AC1: Enforce SessionData status constraints
     */
    @Test
    fun `DTOs with only status serialize to a CBOR object with 1 element`(
        @TestParameter status: SessionDataStatus
    ) {
        val result = SessionDataDto(status = status.code).toCbor(mapper)

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

        val result = SessionDataDto(
            data = data,
            status = SessionDataStatus.SESSION_TERMINATION.code
        ).toCbor(mapper)

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

    @Test
    @TestParameters(valuesProvider = ExampleSessionDataDtoInputs::class)
    fun `Can deserialize a previously serialized DTO`(dto: SessionDataDto) {
        val sessionDataBytes = dto.toCbor(mapper)
        val result = mapper.readValue(sessionDataBytes, SessionDataDto::class.java)

        assertEquals(dto, result)
    }

    @Test
    fun `Can deserialize a valid test data hex string`() {
        val result = mapper.readValue(validSessionDataDtoBytes, SessionDataDto::class.java)
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
