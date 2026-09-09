package uk.gov.onelogin.sharing.verification.document

import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import io.mockk.every
import io.mockk.mockk
import java.io.ByteArrayOutputStream
import java.security.KeyPairGenerator
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import uk.gov.onelogin.sharing.verification.cose.CoseSign1Stubs
import uk.gov.onelogin.sharing.verification.cose.CoseSign1Stubs.coseKeyBytes
import uk.gov.onelogin.sharing.verification.cose.CoseSign1Stubs.coseSign1WithNonNullPayload
import uk.gov.onelogin.sharing.verification.cose.CoseSign1Stubs.emptyDeviceNameSpacesBytes
import uk.gov.onelogin.sharing.verification.cose.CoseSign1Stubs.malformedCoseSign1
import uk.gov.onelogin.sharing.verification.cose.CoseSign1Stubs.sessionTranscriptBytes
import uk.gov.onelogin.sharing.verification.cose.CoseSign1Stubs.validCoseSign1WithNullPayload
import uk.gov.onelogin.sharing.verification.document.cose.CoseKeyDecoder
import uk.gov.onelogin.sharing.verification.format.document.VerifiableDocument
import uk.gov.onelogin.sharing.verification.format.document.device.DeviceKeyInfo
import uk.gov.onelogin.sharing.verification.format.document.device.DeviceSigned
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResultMatchers.hasError
import uk.gov.onelogin.sharing.verification.trust.TrustVerifier

class DeviceAuthVerifierTest {
    private val trustVerifier: TrustVerifier = mockk(relaxed = true)
    private val deviceAuthVerifier =
        DeviceAuthVerifier(trustVerifier, CoseKeyDecoder(), DeviceAuthenticationEncoder())

    private val keyPair = KeyPairGenerator.getInstance("EC")
        .apply { initialize(ECGenParameterSpec("secp256r1")) }
        .generateKeyPair()
    private val publicKey = keyPair.public as ECPublicKey
    private val tag24SessionTranscript = CoseSign1Stubs.wrapInTag24(sessionTranscriptBytes)

    private fun buildDocument(
        deviceSignature: ByteArray = validCoseSign1WithNullPayload,
        deviceNameSpacesBytes: ByteArray = emptyDeviceNameSpacesBytes
    ): VerifiableDocument.WithPresentation {
        val deviceSigned = mockk<DeviceSigned>()
        every { deviceSigned.deviceSignature } returns deviceSignature
        every { deviceSigned.deviceNameSpacesBytes } returns deviceNameSpacesBytes

        val document = mockk<VerifiableDocument.WithPresentation>()
        every { document.deviceSigned } returns deviceSigned
        every { document.docType } returns "org.iso.18013.5.1.mDL"
        every { document.issuerSigned } returns mockk(relaxed = true)
        return document
    }

    @Test
    fun `valid device signature does not throw`() {
        val document = buildDocument()
        val deviceKeyInfo = DeviceKeyInfo(deviceKey = coseKeyBytes(publicKey))

        deviceAuthVerifier.verify(
            document,
            tag24SessionTranscript,
            deviceKeyInfo
        )
    }

    @Test
    fun `non-null payload throws INVALID_DEVICE_SIGNATURE`() {
        val document = buildDocument(deviceSignature = coseSign1WithNonNullPayload)
        val deviceKeyInfo = DeviceKeyInfo(deviceKey = coseKeyBytes(publicKey))

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            deviceAuthVerifier.verify(
                document,
                tag24SessionTranscript,
                deviceKeyInfo
            )
        }
        assertThat(exception, hasError(VerificationError.INVALID_DEVICE_SIGNATURE))
    }

    @Test
    fun `malformed device signature bytes throw INVALID_DEVICE_SIGNATURE`() {
        val document = buildDocument(deviceSignature = malformedCoseSign1)
        val deviceKeyInfo = DeviceKeyInfo(deviceKey = coseKeyBytes(publicKey))

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            deviceAuthVerifier.verify(
                document,
                tag24SessionTranscript,
                deviceKeyInfo
            )
        }
        assertThat(exception, hasError(VerificationError.INVALID_DEVICE_SIGNATURE))
    }

    @Test
    fun `invalid device key throws INVALID_DEVICE_KEY`() {
        val document = buildDocument()
        val deviceKeyInfo = DeviceKeyInfo(
            deviceKey = byteArrayOf(0xFF.toByte(), 0xFE.toByte())
        )

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            deviceAuthVerifier.verify(
                document,
                tag24SessionTranscript,
                deviceKeyInfo
            )
        }
        assertThat(exception, hasError(VerificationError.INVALID_DEVICE_KEY))
    }

    @Test
    fun `null keyAuthorizations skips scope check`() {
        val document = buildDocument()
        val deviceKeyInfo = DeviceKeyInfo(
            deviceKey = coseKeyBytes(publicKey),
            keyAuthorizations = null
        )

        deviceAuthVerifier.verify(
            document,
            tag24SessionTranscript,
            deviceKeyInfo
        )
    }

    @Test
    fun `signature verification failure propagates from TrustVerifier`() {
        every {
            trustVerifier.verifyCOSESign1(any<ByteArray>(), any<ECPublicKey>(), any())
        } throws VerificationResult.Failure(VerificationError.INVALID_DEVICE_SIGNATURE)

        val document = buildDocument()
        val deviceKeyInfo = DeviceKeyInfo(deviceKey = coseKeyBytes(publicKey))

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            deviceAuthVerifier.verify(
                document,
                tag24SessionTranscript,
                deviceKeyInfo
            )
        }
        assertThat(exception, hasError(VerificationError.INVALID_DEVICE_SIGNATURE))
    }

    @Test
    fun `key authorizations violation throws INVALID_DEVICE_KEY`() {
        val nameSpaceMap = ByteArrayOutputStream().also { out ->
            CBORFactory().createGenerator(out).use { gen ->
                gen.writeStartObject(1)
                gen.writeFieldName("unauthorized.namespace")
                gen.writeStartObject(0)
                gen.writeEndObject()
                gen.writeEndObject()
            }
        }.toByteArray()
        val deviceNameSpacesBytes = ByteArrayOutputStream().also { out ->
            CBORFactory().createGenerator(out).use { gen ->
                gen.writeTag(24)
                gen.writeBinary(nameSpaceMap)
            }
        }.toByteArray()

        val document = buildDocument(deviceNameSpacesBytes = deviceNameSpacesBytes)
        val deviceKeyInfo = DeviceKeyInfo(
            deviceKey = coseKeyBytes(publicKey),
            keyAuthorizations = mapOf("org.iso.18013.5.1" to "org.iso.18013.5.1")
        )

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            deviceAuthVerifier.verify(
                document,
                tag24SessionTranscript,
                deviceKeyInfo
            )
        }
        assertThat(exception, hasError(VerificationError.INVALID_DEVICE_KEY))
    }

    @Test
    fun `malformed deviceNameSpacesBytes with keyAuthorizations throws INVALID_DEVICE_KEY`() {
        val document = buildDocument(deviceNameSpacesBytes = byteArrayOf(0xFF.toByte()))
        val deviceKeyInfo = DeviceKeyInfo(
            deviceKey = coseKeyBytes(publicKey),
            keyAuthorizations = mapOf("org.iso.18013.5.1" to "org.iso.18013.5.1")
        )

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            deviceAuthVerifier.verify(
                document,
                tag24SessionTranscript,
                deviceKeyInfo
            )
        }
        assertThat(exception, hasError(VerificationError.INVALID_DEVICE_KEY))
    }
}
