package uk.gov.onelogin.sharing.cryptoService.holder

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import uk.gov.logging.api.v2.Logger
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.cryptoService.FakeSessionSecurity
import uk.gov.onelogin.sharing.cryptoService.cbor.ItemsRequestEncoderStub.MDL_DOC_TYPE
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.SessionTranscriptStub.validSessionTranscript

class DeviceSignatureUseCaseImplTest {

    private val logger = mockk<Logger>(relaxed = true)
    private val deviceSignatureUseCase = DeviceSignatureUseCaseImpl(logger)
    private val holderCryptoService = HolderCryptoServiceImpl(
        sessionSecurity = FakeSessionSecurity(),
        logger = SystemLogger()
    )
    private val cborMapper = ObjectMapper(CBORFactory())

    private val deviceAuthBytes = holderCryptoService.buildDeviceAuthenticationBytes(
        sessionTranscript = validSessionTranscript,
        docType = MDL_DOC_TYPE
    ).deviceAuthenticationBytes

    @Test
    fun `buildDeviceSignedStructures returns COSE_Sign1 array with 4-element header`() {
        val result = deviceSignatureUseCase.buildDeviceSignedStructures(deviceAuthBytes)

        assertEquals(0x84.toByte(), result.coseSign1Array[0])
    }

    @Test
    fun `buildDeviceSignedStructures returns non-empty coseSign1Array, deviceAuth and deviceSigned`() {
        val result = deviceSignatureUseCase.buildDeviceSignedStructures(deviceAuthBytes)

        assertNotNull(result.coseSign1Array)
        assertNotNull(result.deviceAuth)
        assertNotNull(result.deviceSigned)
    }

    @Test
    fun `deviceAuth map contains deviceSignature key`() {
        val result = deviceSignatureUseCase.buildDeviceSignedStructures(deviceAuthBytes)

        val deviceAuthMap: Map<*, *> = cborMapper.readValue(result.deviceAuth, Map::class.java)
        assertTrue(deviceAuthMap.containsKey("deviceSignature"))
    }
}