package uk.gov.onelogin.sharing.orchestration.holder.session

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.security.GeneralSecurityException
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.cryptoService.holder.DeviceSignatureException
import uk.gov.onelogin.sharing.cryptoService.holder.DeviceSignatureResult
import uk.gov.onelogin.sharing.cryptoService.holder.DeviceSignatureUseCase
import uk.gov.onelogin.sharing.orchestration.Credential
import uk.gov.onelogin.sharing.orchestration.CredentialProvider

class HolderResponseUseCaseImplTest {

    private val logger = SystemLogger()
    private val deviceSignatureService = mockk<DeviceSignatureUseCase>()
    private val credentialProvider = mockk<CredentialProvider>()

    private val useCase = HolderResponseUseCaseImpl(
        logger = logger,
        deviceSignatureService = deviceSignatureService
    )

    private val credential = Credential(id = "doc-1", rawCredential = byteArrayOf())
    private val deviceAuthBytes = byteArrayOf(0x01, 0x02, 0x03)
    private val signatureBytes = byteArrayOf(0x04, 0x05, 0x06)
    private val deviceSignedBytes = byteArrayOf(0x07, 0x08)
    private val deviceAuthResultBytes = byteArrayOf(0x09, 0x0a)

    private val signatureResult = DeviceSignatureResult(
        coseSign1Array = byteArrayOf(),
        deviceAuth = deviceAuthResultBytes,
        deviceSigned = deviceSignedBytes
    )

    @Test
    fun `generateDeviceResponse returns DeviceSigned with nameSpaces from deviceSigned`() =
        runTest {
            coEvery {
                credentialProvider.sign(
                    deviceAuthBytes,
                    credential.id
                )
            } returns signatureBytes
            coEvery {
                deviceSignatureService.buildDeviceSignedStructures(signatureBytes)
            } returns signatureResult

            val result =
                useCase.generateDeviceResponse(
                    credential,
                    deviceAuthBytes,
                    credentialProvider
                )

            assertArrayEquals(deviceSignedBytes, result.nameSpaces)
        }

    @Test
    fun `generateDeviceResponse returns DeviceSigned with deviceAuth from signatureResult`() =
        runTest {
            coEvery {
                credentialProvider.sign(
                    deviceAuthBytes,
                    credential.id
                )
            } returns signatureBytes
            coEvery {
                deviceSignatureService.buildDeviceSignedStructures(signatureBytes)
            } returns signatureResult

            val result =
                useCase.generateDeviceResponse(
                    credential,
                    deviceAuthBytes,
                    credentialProvider
                )

            assertArrayEquals(deviceAuthResultBytes, result.deviceAuth)
        }

    @Test
    fun `generateDeviceResponse passes deviceAuthBytes and documentId to credentialProvider`() =
        runTest {
            coEvery {
                credentialProvider.sign(
                    deviceAuthBytes,
                    credential.id
                )
            } returns signatureBytes
            coEvery { deviceSignatureService.buildDeviceSignedStructures(signatureBytes) } returns
                signatureResult

            useCase.generateDeviceResponse(credential, deviceAuthBytes, credentialProvider)

            coVerify { credentialProvider.sign(deviceAuthBytes, credential.id) }
        }

    @Test
    fun `generateDeviceResponse passes signature bytes from provider to deviceSignatureService`() =
        runTest {
            coEvery {
                credentialProvider.sign(
                    deviceAuthBytes,
                    credential.id
                )
            } returns signatureBytes
            coEvery {
                deviceSignatureService.buildDeviceSignedStructures(signatureBytes)
            } returns signatureResult

            useCase.generateDeviceResponse(
                credential,
                deviceAuthBytes,
                credentialProvider
            )

            coVerify { deviceSignatureService.buildDeviceSignedStructures(signatureBytes) }
        }

    @Test
    fun `generateDeviceResponse wraps DeviceSignatureException in DeviceSignatureException`() =
        runTest {
            val cause = DeviceSignatureException("inner failure")
            coEvery { credentialProvider.sign(any(), any()) } returns signatureBytes
            coEvery { deviceSignatureService.buildDeviceSignedStructures(any()) } throws cause

            val thrown = assertFailsWith<DeviceSignatureException> {
                useCase.generateDeviceResponse(credential, deviceAuthBytes, credentialProvider)
            }

            assertEquals("Failed to generate device response", thrown.message)
            assertEquals(cause, thrown.cause)
        }

    @Test
    fun `generateDeviceResponse wraps GeneralSecurityException in DeviceSignatureException`() =
        runTest {
            val cause = GeneralSecurityException("key error")
            coEvery { credentialProvider.sign(any(), any()) } throws cause

            val thrown = assertFailsWith<DeviceSignatureException> {
                useCase.generateDeviceResponse(credential, deviceAuthBytes, credentialProvider)
            }

            assertEquals("Failed to generate device response", thrown.message)
            assertEquals(cause, thrown.cause)
        }
}
