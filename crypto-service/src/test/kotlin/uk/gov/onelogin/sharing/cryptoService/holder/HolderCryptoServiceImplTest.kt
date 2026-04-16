package uk.gov.onelogin.sharing.cryptoService.holder

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.cryptoService.FakeSessionSecurity
import uk.gov.onelogin.sharing.cryptoService.cbor.encodeCbor
import uk.gov.onelogin.sharing.cryptoService.cbor.toDto
import uk.gov.onelogin.sharing.cryptoService.secureArea.session.SessionKeyGenerator.Companion.DeviceRole
import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionData
import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionDataStatus
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponse

class HolderCryptoServiceImplTest {

    private val fakeSessionSecurity = FakeSessionSecurity()
    private val service = HolderCryptoServiceImpl(
        sessionSecurity = fakeSessionSecurity,
        logger = SystemLogger()
    )
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

    @Test
    fun `encryptDeviceResponse encodes to CBOR and encrypts with correct parameters`() {
        val expectedEncrypted = byteArrayOf(0x0A, 0x0B, 0x0C)
        fakeSessionSecurity.encryptedToReturn = expectedEncrypted
        val skDevice = byteArrayOf(0x01, 0x02)
        val encryptCounter = 3u

        val deviceResponse = DeviceResponse(
            documents = null,
            documentErrors = null
        )

        val result = service.encryptDeviceResponse(deviceResponse, skDevice, encryptCounter)

        val expectedCborBytes = deviceResponse.toDto().encodeCbor()
        assertArrayEquals(expectedEncrypted, result)
        assertArrayEquals(skDevice, fakeSessionSecurity.lastEncryptKey)
        assertArrayEquals(expectedCborBytes, fakeSessionSecurity.lastEncryptData)
        assertEquals(DeviceRole.HOLDER, fakeSessionSecurity.lastEncryptRole)
        assertEquals(encryptCounter, fakeSessionSecurity.lastEncryptCounter)
    }
}
