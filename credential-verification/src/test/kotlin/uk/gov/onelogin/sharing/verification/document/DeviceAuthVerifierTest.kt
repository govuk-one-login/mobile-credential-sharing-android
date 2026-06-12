package uk.gov.onelogin.sharing.verification.document

import com.fasterxml.jackson.databind.ObjectMapper
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
import uk.gov.onelogin.sharing.verification.format.document.VerifiableDocument
import uk.gov.onelogin.sharing.verification.format.document.device.DeviceKeyInfo
import uk.gov.onelogin.sharing.verification.format.document.device.DeviceSigned
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResultMatchers.hasError
import uk.gov.onelogin.sharing.verification.trust.TrustVerifier

class DeviceAuthVerifierTest {
    private val cborMapper = ObjectMapper(CBORFactory())
    private val trustVerifier: TrustVerifier = mockk(relaxed = true)
    private val deviceAuthVerifier = DeviceAuthVerifier(trustVerifier)

    private val keyPair = KeyPairGenerator.getInstance("EC")
        .apply { initialize(ECGenParameterSpec("secp256r1")) }
        .generateKeyPair()
    private val publicKey = keyPair.public as ECPublicKey

    private fun buildCoseKeyBytes(key: ECPublicKey = publicKey): ByteArray {
        val x = fixCoordinate(key.w.affineX.toByteArray())
        val y = fixCoordinate(key.w.affineY.toByteArray())
        val node = cborMapper.createObjectNode()
        node.put("1", 2)
        node.put("-1", 1)
        node.put("-2", x)
        node.put("-3", y)
        return cborMapper.writeValueAsBytes(node)
    }

    private fun fixCoordinate(bytes: ByteArray): ByteArray = when {
        bytes.size == 33 && bytes[0] == 0.toByte() -> bytes.copyOfRange(1, 33)
        bytes.size < 32 -> ByteArray(32 - bytes.size) + bytes
        else -> bytes
    }

    private fun buildValidCoseSign1WithNullPayload(): ByteArray {
        val protectedHeader = ByteArrayOutputStream().also { out ->
            CBORFactory().createGenerator(out).use { gen ->
                gen.writeStartObject(1)
                gen.writeFieldId(1)
                gen.writeNumber(-7L)
                gen.writeEndObject()
            }
        }.toByteArray()

        return ByteArrayOutputStream().also { out ->
            CBORFactory().createGenerator(out).use { gen ->
                gen.writeStartArray(null, 4)
                gen.writeBinary(protectedHeader)
                gen.writeStartObject(0)
                gen.writeEndObject()
                gen.writeNull()
                gen.writeBinary(ByteArray(64))
                gen.writeEndArray()
            }
        }.toByteArray()
    }

    private fun buildCoseSign1WithNonNullPayload(): ByteArray {
        val protectedHeader = ByteArrayOutputStream().also { out ->
            CBORFactory().createGenerator(out).use { gen ->
                gen.writeStartObject(1)
                gen.writeFieldId(1)
                gen.writeNumber(-7L)
                gen.writeEndObject()
            }
        }.toByteArray()

        return ByteArrayOutputStream().also { out ->
            CBORFactory().createGenerator(out).use { gen ->
                gen.writeStartArray(null, 4)
                gen.writeBinary(protectedHeader)
                gen.writeStartObject(0)
                gen.writeEndObject()
                gen.writeBinary(byteArrayOf(0x01, 0x02)) // non-null payload
                gen.writeBinary(ByteArray(64))
                gen.writeEndArray()
            }
        }.toByteArray()
    }

    private fun buildEmptyDeviceNameSpacesBytes(): ByteArray {
        val emptyMap = ByteArrayOutputStream().also { out ->
            CBORFactory().createGenerator(out).use { gen ->
                gen.writeStartObject(0)
                gen.writeEndObject()
            }
        }.toByteArray()
        return ByteArrayOutputStream().also { out ->
            CBORFactory().createGenerator(out).use { gen ->
                gen.writeTag(24)
                gen.writeBinary(emptyMap)
            }
        }.toByteArray()
    }

    private fun buildDocument(
        deviceSignature: ByteArray = buildValidCoseSign1WithNullPayload(),
        deviceNameSpacesBytes: ByteArray = buildEmptyDeviceNameSpacesBytes()
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
        val deviceKeyInfo = DeviceKeyInfo(deviceKey = buildCoseKeyBytes())

        deviceAuthVerifier.verifyDeviceAuth(
            document,
            buildSessionTranscriptBytes(),
            deviceKeyInfo
        )
    }

    @Test
    fun `non-null payload throws INVALID_DEVICE_SIGNATURE`() {
        val document = buildDocument(deviceSignature = buildCoseSign1WithNonNullPayload())
        val deviceKeyInfo = DeviceKeyInfo(deviceKey = buildCoseKeyBytes())

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            deviceAuthVerifier.verifyDeviceAuth(
                document,
                buildSessionTranscriptBytes(),
                deviceKeyInfo
            )
        }
        assertThat(exception, hasError(VerificationError.INVALID_DEVICE_SIGNATURE))
    }

    @Test
    fun `malformed device signature bytes throw INVALID_DEVICE_SIGNATURE`() {
        val document = buildDocument(deviceSignature = byteArrayOf(0xFF.toByte(), 0x01))
        val deviceKeyInfo = DeviceKeyInfo(deviceKey = buildCoseKeyBytes())

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            deviceAuthVerifier.verifyDeviceAuth(
                document,
                buildSessionTranscriptBytes(),
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
            deviceAuthVerifier.verifyDeviceAuth(
                document,
                buildSessionTranscriptBytes(),
                deviceKeyInfo
            )
        }
        assertThat(exception, hasError(VerificationError.INVALID_DEVICE_KEY))
    }

    @Test
    fun `null keyAuthorizations skips scope check`() {
        val document = buildDocument()
        val deviceKeyInfo = DeviceKeyInfo(
            deviceKey = buildCoseKeyBytes(),
            keyAuthorizations = null
        )

        deviceAuthVerifier.verifyDeviceAuth(
            document,
            buildSessionTranscriptBytes(),
            deviceKeyInfo
        )
        // no INVALID_DEVICE_KEY thrown
    }

    @Test
    fun `signature verification failure propagates from TrustVerifier`() {
        every {
            trustVerifier.verifyCOSESign1(any<ByteArray>(), any<ECPublicKey>(), any())
        } throws VerificationResult.Failure(VerificationError.INVALID_DEVICE_SIGNATURE)

        val document = buildDocument()
        val deviceKeyInfo = DeviceKeyInfo(deviceKey = buildCoseKeyBytes())

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            deviceAuthVerifier.verifyDeviceAuth(
                document,
                buildSessionTranscriptBytes(),
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
            deviceKey = buildCoseKeyBytes(),
            keyAuthorizations = mapOf("org.iso.18013.5.1" to "org.iso.18013.5.1")
        )

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            deviceAuthVerifier.verifyDeviceAuth(
                document,
                buildSessionTranscriptBytes(),
                deviceKeyInfo
            )
        }
        assertThat(exception, hasError(VerificationError.INVALID_DEVICE_KEY))
    }

    private fun buildSessionTranscriptBytes(): ByteArray {
        return ByteArrayOutputStream().also { out ->
            CBORFactory().createGenerator(out).use { gen ->
                gen.writeStartArray(null, 3)
                gen.writeNull()
                gen.writeNull()
                gen.writeNull()
                gen.writeEndArray()
            }
        }.toByteArray()
    }
}
