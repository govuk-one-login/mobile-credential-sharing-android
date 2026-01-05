package uk.gov.onelogin.sharing.security.cbor.deserializers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlin.test.assertEquals
import org.junit.Before
import org.junit.Test
import uk.gov.onelogin.sharing.security.SessionEstablishmentStub.MOCK_SESSION_ESTABLISHMENT_DATA
import uk.gov.onelogin.sharing.security.cbor.serializers.EmbeddedCbor
import uk.gov.onelogin.sharing.security.cbor.serializers.EmbeddedCborSerializer

class SessionEstablishmentDeserializerTest {

    private lateinit var cborMapper: ObjectMapper
    private lateinit var deserializer: SessionEstablishmentDeserializer

    @Before
    fun setUp() {
        cborMapper = ObjectMapper(CBORFactory()).apply {
            registerKotlinModule()
            val module =
                SimpleModule().addSerializer(EmbeddedCbor::class.java, EmbeddedCborSerializer())
            registerModule(module)
        }
        deserializer = SessionEstablishmentDeserializer()
    }

    @Test
    fun `should prefix eReaderKey with CBOR Tag 24 after deserializing raw bytes`() {
        val cborData = MOCK_SESSION_ESTABLISHMENT_DATA.hexToByteArray()

        val jsonParser = cborMapper.factory.createParser(cborData)
        val context = cborMapper.deserializationContext

        val actualSessionEstablishmentDto = deserializer.deserialize(jsonParser, context)

        val embeddedReaderBytes = EmbeddedCbor(actualSessionEstablishmentDto.eReaderKey.encoded)

        val eReaderKeyBytes = cborMapper.writeValueAsBytes(embeddedReaderBytes)

        assertEquals(0xD8.toByte(), eReaderKeyBytes[0])
        assertEquals(0x18.toByte(), eReaderKeyBytes[1])
    }
}
