package uk.gov.onelogin.sharing.orchestration.holder.session

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import uk.gov.onelogin.sharing.cryptoService.DeviceRequestStub
import uk.gov.onelogin.sharing.cryptoService.holder.DeviceAuthenticationResult
import uk.gov.onelogin.sharing.cryptoService.holder.DeviceSignatureException
import uk.gov.onelogin.sharing.cryptoService.holder.FakeHolderCryptoService
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceSigned
import uk.gov.onelogin.sharing.orchestration.Credential
import uk.gov.onelogin.sharing.orchestration.CredentialProvider
import uk.gov.onelogin.sharing.orchestration.CredentialRequest

class ConfirmConsentUseCaseImplTest {

    private val fakeHolderCryptoService = FakeHolderCryptoService()
    private val fakeHolderResponseUseCase = FakeHolderResponseUseCase()

    private val useCase = ConfirmConsentUseCaseImpl(
        holderCryptoService = fakeHolderCryptoService,
        holderResponseUseCase = fakeHolderResponseUseCase
    )

    private val sessionTranscript = byteArrayOf(0x01, 0x02)
    private val deviceAuthBytes = byteArrayOf(0x03, 0x04)
    private val credential = Credential(id = "doc-1", rawCredential = byteArrayOf())
    private val deviceRequest = DeviceRequestStub.deviceRequestStub

    private val docType = deviceRequest.docRequests.first().itemsRequest.docType

    private val credentialProvider = object : CredentialProvider {
        override suspend fun getCredentials(request: CredentialRequest) = listOf(credential)
        override suspend fun sign(payload: ByteArray, documentId: String) = byteArrayOf()
    }

    @Test
    fun `execute returns DeviceSigned from holderResponseUseCase`() = runTest {
        val expected = DeviceSigned(nameSpaces = byteArrayOf(0x01), deviceAuth = byteArrayOf(0x02))
        fakeHolderCryptoService.deviceAuthResultToReturn = DeviceAuthenticationResult(
            deviceAuthenticationBytes = deviceAuthBytes,
            deviceNameSpacesBytes = byteArrayOf()
        )
        fakeHolderResponseUseCase.deviceSignedToReturn = expected

        val result = useCase.execute(sessionTranscript, deviceRequest, credentialProvider)

        assertArrayEquals(expected.nameSpaces, result.nameSpaces)
        assertArrayEquals(expected.deviceAuth, result.deviceAuth)
    }

    @Test
    fun `execute passes sessionTranscript and docType to buildDeviceAuthenticationBytes`() =
        runTest {
            useCase.execute(sessionTranscript, deviceRequest, credentialProvider)

            assertArrayEquals(
                sessionTranscript,
                fakeHolderCryptoService.lastDeviceAuthSessionTranscript
            )
            assertEquals(docType, fakeHolderCryptoService.lastDeviceAuthDocType)
        }

    @Test
    fun `execute passes deviceAuthenticationBytes and credential to holderResponseUseCase`() =
        runTest {
            fakeHolderCryptoService.deviceAuthResultToReturn = DeviceAuthenticationResult(
                deviceAuthenticationBytes = deviceAuthBytes,
                deviceNameSpacesBytes = byteArrayOf()
            )

            useCase.execute(sessionTranscript, deviceRequest, credentialProvider)

            assertArrayEquals(
                deviceAuthBytes,
                fakeHolderResponseUseCase.lastDeviceAuthenticationBytes
            )
            assertEquals(credential, fakeHolderResponseUseCase.lastSelectedCredential)
        }

    @Test
    fun `execute throws DeviceSignatureException when docRequests is empty`() {
        val emptyRequest =
            DeviceRequestStub.deviceRequest(emptyMap()).copy(docRequests = emptyList())

        assertThrows(DeviceSignatureException::class.java) {
            runTest { useCase.execute(sessionTranscript, emptyRequest, credentialProvider) }
        }
    }

    @Test
    fun `execute throws DeviceSignatureException when no credentials available`() {
        val noCredentialProvider = object : CredentialProvider {
            override suspend fun getCredentials(request: CredentialRequest) =
                emptyList<Credential>()

            override suspend fun sign(payload: ByteArray, documentId: String) = byteArrayOf()
        }

        assertThrows(DeviceSignatureException::class.java) {
            runTest { useCase.execute(sessionTranscript, deviceRequest, noCredentialProvider) }
        }
    }

    @Test
    fun `execute propagates DeviceSignatureException from holderResponseUseCase`() {
        fakeHolderResponseUseCase.exception = DeviceSignatureException("sign failed")

        assertThrows(DeviceSignatureException::class.java) {
            runTest { useCase.execute(sessionTranscript, deviceRequest, credentialProvider) }
        }
    }
}
