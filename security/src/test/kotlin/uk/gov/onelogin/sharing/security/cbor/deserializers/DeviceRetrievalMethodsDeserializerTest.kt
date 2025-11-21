package uk.gov.onelogin.sharing.security.cbor.deserializers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import uk.gov.onelogin.sharing.security.DeviceRetrievalMethodsDeserializerStub.expectedMethods
import uk.gov.onelogin.sharing.security.DeviceRetrievalMethodsDeserializerStub.rawCborMock

class DeviceRetrievalMethodsDeserializerTest {

    private lateinit var cborMapper: ObjectMapper
    private lateinit var deserializer: DeviceRetrievalMethodsDeserializer

    @Before
    fun setUp() {
        cborMapper = ObjectMapper(CBORFactory()).apply {
            registerKotlinModule()
        }
        deserializer = DeviceRetrievalMethodsDeserializer()
    }

    @Test
    fun `deserializes raw CBOR data to DeviceRetrievalMethodsDto`() {
        val cborData = cborMapper.writeValueAsBytes(rawCborMock)

        val jsonParser = cborMapper.factory.createParser(cborData)
        val context = cborMapper.deserializationContext
        val result = deserializer.deserialize(jsonParser, context)

        assertEquals(expectedMethods, result)
    }
}
