package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment

import com.google.testing.junit.testparameterinjector.KotlinTestParameters.namedTestValues
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMapper
import uk.gov.onelogin.sharing.models.mdoc.cbor.serializers.EmbeddedCbor
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.SessionEstablishmentStubs.validSessionEstablishmentDto
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.SessionEstablishmentStubs.validSessionEstablishmentDtoBytes

@RunWith(TestParameterInjector::class)
class SessionEstablishmentDtoTest {

    private val objectMapper = CborMapper.default

    private val equalityContract = SessionEstablishmentDto(
        eReaderKey = EmbeddedCbor(byteArrayOf(0, 1)),
        data = byteArrayOf(1, 2)
    )
    private val differentKey = equalityContract.copy(
        eReaderKey = EmbeddedCbor(byteArrayOf(1, 2))
    )
    private val differentData = equalityContract.copy(
        data = byteArrayOf(2, 3)
    )

    @Test
    fun `Equality contract`() {
        assertEquals(equalityContract, equalityContract)
        assertEquals(equalityContract, equalityContract.copy())

        assertFalse(equalityContract.equals(null))
        assertFalse(equalityContract.equals("different class"))
        assertNotEquals(equalityContract, differentKey)
        assertNotEquals(equalityContract, differentData)
    }

    @Test
    fun `Hashcode contract`() {
        assertEquals(equalityContract.hashCode(), equalityContract.hashCode())
        assertEquals(equalityContract.hashCode(), equalityContract.copy().hashCode())

        assertNotEquals(equalityContract.hashCode(), differentKey.hashCode())
        assertNotEquals(equalityContract.hashCode(), differentData.hashCode())
    }

    @Test
    fun `Serialization process`(
        @TestParameter deserialized: ByteArray = namedTestValues(
            "Mapper writing values" to objectMapper.writeValueAsBytes(equalityContract),
            "CborEncodable implementation" to equalityContract.toCbor(objectMapper)
        )
    ) {
        val result = objectMapper.readValue(
            deserialized,
            SessionEstablishmentDto::class.java
        )

        assertThat(
            result,
            equalTo(equalityContract)
        )
    }

    @Test
    fun `Stub value matches example input hexadecimal`() {
        val result = objectMapper.readValue(
            validSessionEstablishmentDtoBytes,
            SessionEstablishmentDto::class.java
        )

        assertThat(
            result,
            equalTo(validSessionEstablishmentDto)
        )
    }
}
