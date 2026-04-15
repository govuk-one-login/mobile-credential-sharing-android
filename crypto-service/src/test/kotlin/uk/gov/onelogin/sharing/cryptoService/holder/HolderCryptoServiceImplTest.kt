package uk.gov.onelogin.sharing.cryptoService.holder

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import uk.gov.onelogin.sharing.cryptoService.cbor.encodeCbor
import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionData
import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionDataStatus

class HolderCryptoServiceImplTest {

    private val service = HolderCryptoServiceImpl()
    private val cborMapper = ObjectMapper(CBORFactory())

    @Test
    fun `buildTerminationSessionData matches directly encoded SessionData`() {
        val result = service.buildTerminationSessionData(SessionDataStatus.SESSION_TERMINATION)
        val expected = SessionData(status = SessionDataStatus.SESSION_TERMINATION).encodeCbor()

        assertEquals(expected.toHexString(), result.toHexString())
    }

    @Test
    fun `buildTerminationSessionData contains status 20 and no data`() {
        val result = service.buildTerminationSessionData(SessionDataStatus.SESSION_TERMINATION)
        val map: Map<*, *> = cborMapper.readValue(result, Map::class.java)

        assertEquals(SessionDataStatus.SESSION_TERMINATION.code.toInt(), map["status"])
        assertFalse(map.containsKey("data"))
    }
}
