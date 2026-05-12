package uk.gov.onelogin.sharing.orchestration.holder.session

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
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
import uk.gov.onelogin.sharing.orchestration.CredentialProvider
import uk.gov.onelogin.sharing.orchestration.holder.credential.ValidatedCredential

class HolderResponseUseCaseImplTest {

    private val logger = SystemLogger()
    private val deviceSignatureService = mockk<DeviceSignatureUseCase> {
        every { buildCoseSignStructure(any()) } returns byteArrayOf(0x01)
    }
    private val credentialProvider = mockk<CredentialProvider>()

    private val useCase = HolderResponseUseCaseImpl(
        logger = logger,
        deviceSignatureService = deviceSignatureService,
        credentialProvider = credentialProvider
    )

    private val validatedCredential = ValidatedCredential(
        credentialId = "doc-1",
        nameSpaces = byteArrayOf(),
        issuerAuth = byteArrayOf()
    )
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
    fun `generateDeviceResponse returns DeviceSigned with empty CBOR map nameSpaces`() = runTest {
        coEvery {
            credentialProvider.sign(
                any(),
                validatedCredential.credentialId
            )
        } returns signatureBytes
        coEvery {
            deviceSignatureService.buildDeviceSignedStructures(signatureBytes)
        } returns signatureResult

        val result = useCase.generateDeviceResponse(
            validatedCredential,
            deviceAuthBytes
        )

        assertArrayEquals(byteArrayOf(0xA0.toByte()), result.nameSpaces)
    }

    @Test
    fun `generateDeviceResponse returns DeviceSigned with deviceAuth from signatureResult`() =
        runTest {
            coEvery {
                credentialProvider.sign(
                    any(),
                    validatedCredential.credentialId
                )
            } returns signatureBytes
            coEvery {
                deviceSignatureService.buildDeviceSignedStructures(signatureBytes)
            } returns signatureResult

            val result = useCase.generateDeviceResponse(
                validatedCredential,
                deviceAuthBytes
            )

            assertArrayEquals(deviceAuthResultBytes, result.deviceAuth)
        }

    @Test
    fun `generateDeviceResponse passes deviceAuthBytes and credentialId to credentialProvider`() =
        runTest {
            coEvery {
                credentialProvider.sign(
                    any(),
                    validatedCredential.credentialId
                )
            } returns signatureBytes
            coEvery {
                deviceSignatureService.buildDeviceSignedStructures(signatureBytes)
            } returns signatureResult

            useCase.generateDeviceResponse(
                validatedCredential,
                deviceAuthBytes
            )

            coVerify {
                credentialProvider.sign(
                    any(),
                    validatedCredential.credentialId
                )
            }
        }

    @Test
    fun `generateDeviceResponse passes signature bytes from provider to deviceSignatureService`() =
        runTest {
            coEvery {
                credentialProvider.sign(
                    any(),
                    validatedCredential.credentialId
                )
            } returns signatureBytes
            coEvery { deviceSignatureService.buildDeviceSignedStructures(signatureBytes) } returns
                signatureResult

            useCase.generateDeviceResponse(validatedCredential, deviceAuthBytes)

            coVerify { deviceSignatureService.buildDeviceSignedStructures(signatureBytes) }
        }

    @Test
    fun `generateDeviceResponse wraps DeviceSignatureException in DeviceSignatureException`() =
        runTest {
            val cause = DeviceSignatureException("inner failure")
            coEvery { credentialProvider.sign(any(), any()) } returns signatureBytes
            coEvery { deviceSignatureService.buildDeviceSignedStructures(any()) } throws cause

            val thrown = assertFailsWith<DeviceSignatureException> {
                useCase.generateDeviceResponse(validatedCredential, deviceAuthBytes)
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
                useCase.generateDeviceResponse(validatedCredential, deviceAuthBytes)
            }

            assertEquals("Failed to generate device response", thrown.message)
            assertEquals(cause, thrown.cause)
        }
}
