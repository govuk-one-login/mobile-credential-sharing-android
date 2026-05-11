package uk.gov.onelogin.sharing.orchestration.verifier.session

import kotlin.test.assertFailsWith
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.cryptoService.verifier.EncryptDeviceRequestException
import uk.gov.onelogin.sharing.cryptoService.verifier.FakeEncryptDeviceRequestUseCase
import uk.gov.onelogin.sharing.orchestration.verificationrequest.AttributeGroup
import uk.gov.onelogin.sharing.orchestration.verificationrequest.MdlAttribute
import uk.gov.onelogin.sharing.orchestration.verificationrequest.VerificationRequest

class BuildDeviceRequestUseCaseImplTest {

    private val logger = SystemLogger()
    private val skReader = ByteArray(32) { 0x01 }
    private val encryptCounter = 1u
    private val encryptedResult = byteArrayOf(0xAA.toByte(), 0xBB.toByte())

    private val fakeEncrypt = FakeEncryptDeviceRequestUseCase(
        encryptedToReturn = encryptedResult
    )

    private val useCase = BuildDeviceRequestUseCaseImpl(fakeEncrypt, logger)

    private val verificationRequest = VerificationRequest(
        documentType = "org.iso.18013.5.1.mDL",
        attributeGroup = AttributeGroup(
            mapOf(MdlAttribute.FamilyName to true)
        )
    )

    @Test
    fun `execute passes skReader and encryptCounter to encrypt`() {
        useCase.execute(verificationRequest, skReader, encryptCounter)

        assertArrayEquals(skReader, fakeEncrypt.lastSkReader)
        assertEquals(encryptCounter, fakeEncrypt.lastEncryptCounter)
    }

    @Test
    fun `execute returns encrypted bytes from encrypt use case`() {
        val result = useCase.execute(verificationRequest, skReader, encryptCounter)

        assertArrayEquals(encryptedResult, result)
    }

    @Test
    fun `execute logs ItemsRequest`() {
        useCase.execute(verificationRequest, skReader, encryptCounter)

        assert(logger.any { it.message.startsWith("ItemsRequest:") })
    }

    @Test
    fun `execute propagates EncryptDeviceRequestException`() {
        val failingEncrypt = FakeEncryptDeviceRequestUseCase(
            exceptionToThrow = EncryptDeviceRequestException(
                "Error encrypting DeviceRequest",
                RuntimeException("AES failure")
            )
        )
        val useCase = BuildDeviceRequestUseCaseImpl(failingEncrypt, logger)

        assertFailsWith<EncryptDeviceRequestException> {
            useCase.execute(verificationRequest, skReader, encryptCounter)
        }
    }
}
