package uk.gov.onelogin.sharing.orchestration.verifier.credential

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import uk.gov.onelogin.sharing.cryptoService.verifier.EncryptDeviceRequestException
import uk.gov.onelogin.sharing.orchestration.verifier.session.FakeBuildDeviceRequestUseCase
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierConfigStub.photoAndAgeOver21Config
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierConfigStub.nameRetainAndAgeOver18Config
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierConfigStub.verifierConfigStub
import uk.gov.onelogin.sharing.cryptoService.cbor.ItemsRequestEncoderStub.MDL_DOC_TYPE
import uk.gov.onelogin.sharing.cryptoService.cbor.ItemsRequestEncoderStub.MDL_NAMESPACE
import uk.gov.onelogin.sharing.orchestration.verificationrequest.toItemsRequest
import kotlin.test.assertFailsWith

class DeviceRequestHandlerImplTest {

    private val skReader = ByteArray(32) { 0x01 }
    private val encryptCounter = 1u
    private val encryptedResult = byteArrayOf(0xAA.toByte(), 0xBB.toByte())

    private val fakeBuildUseCase = FakeBuildDeviceRequestUseCase(
        encryptedToReturn = encryptedResult
    )

    private val handler = DeviceRequestHandlerImpl(
        verifierConfig = verifierConfigStub,
        buildDeviceRequestUseCase = fakeBuildUseCase
    )

    @Test
    fun `buildAndEncrypt passes skReader and encryptCounter to use case`() {
        handler.buildAndEncrypt(skReader, encryptCounter)

        assertArrayEquals(skReader, fakeBuildUseCase.lastSkReader)
        assertEquals(encryptCounter, fakeBuildUseCase.lastEncryptCounter)
    }

    @Test
    fun `buildAndEncrypt passes verificationRequest from config to use case`() {
        handler.buildAndEncrypt(skReader, encryptCounter)

        assertEquals(
            verifierConfigStub.verificationRequest,
            fakeBuildUseCase.lastVerificationRequest
        )
    }

    @Test
    fun `buildAndEncrypt returns encrypted bytes from use case`() {
        val result = handler.buildAndEncrypt(skReader, encryptCounter)

        assertArrayEquals(encryptedResult, result)
    }

    @Test
    fun `buildAndEncrypt propagates EncryptDeviceRequestException`() {
        val failingUseCase = FakeBuildDeviceRequestUseCase(
            exceptionToThrow = EncryptDeviceRequestException(
                "Error encrypting DeviceRequest",
                RuntimeException("AES failure")
            )
        )
        val handler = DeviceRequestHandlerImpl(verifierConfigStub, failingUseCase)

        assertFailsWith<EncryptDeviceRequestException> {
            handler.buildAndEncrypt(skReader, encryptCounter)
        }
    }

    @Test
    fun `buildAndEncrypt passes Photo and Age Over 21 verificationRequest`() {
        val handler = DeviceRequestHandlerImpl(photoAndAgeOver21Config, fakeBuildUseCase)
        handler.buildAndEncrypt(skReader, encryptCounter)

        assertEquals(
            "ItemsRequest(docType=$MDL_DOC_TYPE, " +
                "nameSpaces={$MDL_NAMESPACE={portrait=false, age_over_21=false}})",
            fakeBuildUseCase.lastVerificationRequest
                ?.attributeGroup
                ?.toItemsRequest(fakeBuildUseCase.lastVerificationRequest!!.documentType)
                .toString()
        )
    }

    @Test
    fun `buildAndEncrypt passes Name Retain and Age Over 18 verificationRequest`() {
        val handler = DeviceRequestHandlerImpl(nameRetainAndAgeOver18Config, fakeBuildUseCase)
        handler.buildAndEncrypt(skReader, encryptCounter)

        assertEquals(
            "ItemsRequest(docType=$MDL_DOC_TYPE, " +
                "nameSpaces={$MDL_NAMESPACE={given_name=true, family_name=true, age_over_18=false}})",
            fakeBuildUseCase.lastVerificationRequest
                ?.attributeGroup
                ?.toItemsRequest(fakeBuildUseCase.lastVerificationRequest!!.documentType)
                .toString()
        )
    }
}
