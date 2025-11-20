package uk.gov.onelogin.sharing.security.cbor

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.ByteArrayOutputStream
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import uk.gov.onelogin.sharing.security.SecurityDeserializerStub.coseKeyMap
import uk.gov.onelogin.sharing.security.SecurityDeserializerStub.expectedSecurityDto
import uk.gov.onelogin.sharing.security.cbor.deserializers.SecurityDeserializer
import uk.gov.onelogin.sharing.security.cbor.serializers.EmbeddedCbor
import uk.gov.onelogin.sharing.security.cbor.serializers.EmbeddedCborSerializer

class SecurityDeserializerTest {

    private lateinit var cborMapper: ObjectMapper
    private lateinit var deserializer: SecurityDeserializer

    @Before
    fun setUp() {
        cborMapper = ObjectMapper(CBORFactory()).apply {
            registerKotlinModule()
            val module =
                SimpleModule().addSerializer(EmbeddedCbor::class.java, EmbeddedCborSerializer())
            registerModule(module)
        }
        deserializer = SecurityDeserializer()
    }

    @Test
    fun `should deserialize embedded CBOR data to SecurityDto`() {
        // Not able to register kotlin module in stub otherwise would have moved logic there
        val coseKeyBytes = cborMapper.writeValueAsBytes(coseKeyMap)

        val embeddedCbor = EmbeddedCbor(coseKeyBytes)

        val outerCborList = listOf(
            expectedSecurityDto.cipherSuiteIdentifier,
            embeddedCbor
        )

        val cborData = cborMapper.writeValueAsBytes(outerCborList)

        val jsonParser = cborMapper.factory.createParser(cborData)
        val context = cborMapper.deserializationContext
        val actualSecurityDto = deserializer.deserialize(jsonParser, context)

        Assert.assertEquals(expectedSecurityDto, actualSecurityDto)
    }
}
