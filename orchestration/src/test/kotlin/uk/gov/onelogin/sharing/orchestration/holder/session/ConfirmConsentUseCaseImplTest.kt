package uk.gov.onelogin.sharing.orchestration.holder.session

import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import uk.gov.onelogin.sharing.cryptoService.DeviceRequestStub
import uk.gov.onelogin.sharing.cryptoService.holder.DeviceAuthenticationResult
import uk.gov.onelogin.sharing.cryptoService.holder.DeviceSignatureException
import uk.gov.onelogin.sharing.cryptoService.holder.FakeHolderCryptoService
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceSigned
import uk.gov.onelogin.sharing.orchestration.holder.credential.ValidatedCredential

class ConfirmConsentUseCaseImplTest {

    private val fakeHolderCryptoService = FakeHolderCryptoService()
    private val fakeHolderResponseUseCase = FakeHolderResponseUseCase()

    private val useCase = ConfirmConsentUseCaseImpl(
        holderCryptoService = fakeHolderCryptoService,
        holderResponseUseCase = fakeHolderResponseUseCase
    )

    private val sessionTranscript = byteArrayOf(0x01, 0x02)
    private val deviceAuthBytes = byteArrayOf(0x03, 0x04)
    private val deviceRequest = DeviceRequestStub.deviceRequestStub
    private val docType = deviceRequest.docRequests.first().itemsRequest.docType

    private val validatedCredential = ValidatedCredential(
        credentialId = "doc-1",
        nameSpaces = byteArrayOf(),
        issuerAuth = byteArrayOf()
    )

    @Test
    fun `execute returns DeviceSigned from holderResponseUseCase`() = runTest {
        val expected = DeviceSigned(nameSpaces = byteArrayOf(0x01), deviceAuth = byteArrayOf(0x02))
        fakeHolderCryptoService.deviceAuthResultToReturn = DeviceAuthenticationResult(
            deviceAuthenticationBytes = deviceAuthBytes,
            deviceNameSpacesBytes = byteArrayOf()
        )
        fakeHolderResponseUseCase.deviceSignedToReturn = expected

        val result = useCase.execute(sessionTranscript, deviceRequest, validatedCredential)

        assertArrayEquals(expected.nameSpaces, result.nameSpaces)
        assertArrayEquals(expected.deviceAuth, result.deviceAuth)
    }

    @Test
    fun `execute passes sessionTranscript and docType to buildDeviceAuthenticationBytes`() =
        runTest {
            useCase.execute(sessionTranscript, deviceRequest, validatedCredential)

            assertArrayEquals(
                sessionTranscript,
                fakeHolderCryptoService.lastDeviceAuthSessionTranscript
            )
            assertEquals(docType, fakeHolderCryptoService.lastDeviceAuthDocType)
        }

    @Test
    fun `execute passes devAuthBytes and validatedCredential to holderResponseUseCase`() = runTest {
        fakeHolderCryptoService.deviceAuthResultToReturn = DeviceAuthenticationResult(
            deviceAuthenticationBytes = deviceAuthBytes,
            deviceNameSpacesBytes = byteArrayOf()
        )

        useCase.execute(sessionTranscript, deviceRequest, validatedCredential)

        assertArrayEquals(
            deviceAuthBytes,
            fakeHolderResponseUseCase.lastDeviceAuthenticationBytes
        )
        assertEquals(validatedCredential, fakeHolderResponseUseCase.lastValidatedCredential)
    }

    @Test
    fun `execute throws DeviceSignatureException when docRequests is empty`() = runTest {
        val emptyRequest = DeviceRequestStub.deviceRequest(
            emptyMap()
        ).copy(docRequests = emptyList())

        assertFailsWith<DeviceSignatureException> {
            useCase.execute(sessionTranscript, emptyRequest, validatedCredential)
        }
    }

    @Test
    fun `execute propagates DeviceSignatureException from holderResponseUseCase`() = runTest {
        fakeHolderResponseUseCase.exception = DeviceSignatureException("sign failed")

        assertFailsWith<DeviceSignatureException> {
            useCase.execute(sessionTranscript, deviceRequest, validatedCredential)
        }
    }
}
