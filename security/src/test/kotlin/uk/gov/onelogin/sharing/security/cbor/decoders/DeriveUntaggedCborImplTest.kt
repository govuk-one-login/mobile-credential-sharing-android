package uk.gov.onelogin.sharing.security.cbor.decoders

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import kotlin.test.assertEquals
import org.junit.Before
import org.junit.Test
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.security.SessionEstablishmentStub
import uk.gov.onelogin.sharing.security.SessionEstablishmentStub.MOCK_E_READER_KEY
import uk.gov.onelogin.sharing.security.SessionEstablishmentStub.MOCK_SESSION_ESTABLISHMENT_DATA
import uk.gov.onelogin.sharing.security.cbor.decodeSessionEstablishmentModel
import uk.gov.onelogin.sharing.security.toSessionEstablishment

class DeriveUntaggedCborImplTest {

    private lateinit var cborMapper: ObjectMapper
    private lateinit var deriveUntaggedCbor: DeriveUntaggedCbor
    private val logger = SystemLogger()

    @Before
    fun setUp() {
        cborMapper = ObjectMapper(CBORFactory())
        deriveUntaggedCbor = DeriveUntaggedCborImpl()
    }

    @Test
    fun `deriveUntaggedCbor should return the untagged CBOR`() {
        val sessionEstablishment = decodeSessionEstablishmentModel(
            MOCK_SESSION_ESTABLISHMENT_DATA.hexToByteArray(),
            logger
        ).toSessionEstablishment()

        val untaggedCbor = deriveUntaggedCbor.deriveUntaggedCbor(sessionEstablishment.eReaderKey)
        val untaggedCborToHex = untaggedCbor.toHexString()
        assertEquals(SessionEstablishmentStub.E_READER_KEY_UNTAGGED, untaggedCborToHex)
    }

    @Test
    fun `derive untagged e reader key`() {
        val tagged = MOCK_E_READER_KEY.hexToByteArray()
        val untagged = deriveUntaggedCbor.deriveUntaggedCbor(tagged)

        assertEquals(0xA4.toByte(), untagged[0])
    }
}
