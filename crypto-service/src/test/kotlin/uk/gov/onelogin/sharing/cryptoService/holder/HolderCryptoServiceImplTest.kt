package uk.gov.onelogin.sharing.cryptoService.holder

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.cryptoService.FakeSessionSecurity
import uk.gov.onelogin.sharing.cryptoService.cbor.ItemsRequestEncoderStub.MDL_DOC_TYPE
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.SessionTranscriptStub.validSessionTranscript
import uk.gov.onelogin.sharing.cryptoService.cbor.encodeCbor
import uk.gov.onelogin.sharing.cryptoService.cbor.toDto
import uk.gov.onelogin.sharing.cryptoService.secureArea.session.SessionKeyGenerator.Companion.DeviceRole
import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionData
import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionDataStatus
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponse
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.Document
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.Status

class HolderCryptoServiceImplTest {

    private val fakeSessionSecurity = FakeSessionSecurity()
    private val logger = SystemLogger()
    private val service = HolderCryptoServiceImpl(
        sessionSecurity = fakeSessionSecurity,
        logger = logger
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
    fun `buildDeviceResponse encodes to CBOR and encrypts with correct parameters`() {
        val expectedEncrypted = byteArrayOf(0x0A, 0x0B, 0x0C)
        fakeSessionSecurity.encryptedToReturn = expectedEncrypted
        val skDevice = byteArrayOf(0x01, 0x02)
        val encryptCounter = 3u

        val documents = emptyList<Document>()

        val result = service.buildDeviceResponse(documents, skDevice, encryptCounter)

        val expectedCborBytes = DeviceResponse(
            documents = documents,
            documentErrors = null
        ).toDto().encodeCbor()
        val expectedSessionData = SessionData(data = expectedEncrypted).encodeCbor()
        assertArrayEquals(expectedSessionData, result)
        assertArrayEquals(skDevice, fakeSessionSecurity.lastEncryptKey)
        assertArrayEquals(expectedCborBytes, fakeSessionSecurity.lastEncryptData)
        assertEquals(DeviceRole.HOLDER, fakeSessionSecurity.lastEncryptRole)
        assertEquals(encryptCounter, fakeSessionSecurity.lastEncryptCounter)
    }

    @Test
    fun `buildErrorSessionData contains encrypted payload and status 20`() {
        fakeSessionSecurity.encryptedToReturn = byteArrayOf(0x0A, 0x0B)
        val skDevice = byteArrayOf(0x01, 0x02)

        val result = service.buildErrorSessionData(
            deviceResponseStatus = Status.CBOR_DECODING_ERROR,
            sessionDataStatus = SessionDataStatus.SESSION_TERMINATION,
            skDevice = skDevice,
            encryptCounter = 1u
        )

        val map: Map<*, *> = cborMapper.readValue(result, Map::class.java)
        assertEquals(SessionDataStatus.SESSION_TERMINATION.code.toInt(), map["status"])
        assertTrue(map.containsKey("data"))
        assertEquals(DeviceRole.HOLDER, fakeSessionSecurity.lastEncryptRole)
        assertEquals(1u, fakeSessionSecurity.lastEncryptCounter)
    }

    @Test
    fun `buildDeviceAuthenticationBytes returns a DeviceAuthenticationResult`() {
        val result = service.buildDeviceAuthenticationBytes(
            sessionTranscript = validSessionTranscript,
            docType = MDL_DOC_TYPE
        )

        assertTrue(result.deviceNameSpacesBytes.isNotEmpty())
        assertTrue(
            "DeviceNameSpacesBytes encoded: ${result.deviceNameSpacesBytes.toHexString()}" in
                logger
        )

        assertTrue(result.deviceAuthenticationBytes.isNotEmpty())
        assertTrue(
            "DeviceAuthenticationBytes encoded: ${result.deviceAuthenticationBytes.toHexString()}"
                in logger
        )
    }
}
