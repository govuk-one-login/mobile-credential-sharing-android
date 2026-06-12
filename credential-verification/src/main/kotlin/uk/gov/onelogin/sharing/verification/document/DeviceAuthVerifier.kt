package uk.gov.onelogin.sharing.verification.document

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BinaryNode
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import dev.zacsweers.metro.Inject
import java.io.ByteArrayOutputStream
import uk.gov.onelogin.sharing.verification.document.cose.CoseKeyDecoder
import uk.gov.onelogin.sharing.verification.format.document.VerifiableDocument
import uk.gov.onelogin.sharing.verification.format.document.device.DeviceKeyInfo
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult
import uk.gov.onelogin.sharing.verification.trust.TrustVerifier

@Inject
class DeviceAuthVerifier(
    private val trustVerifier: TrustVerifier
) {
    private val cborMapper = ObjectMapper(CBORFactory())
    private val coseKeyDecoder = CoseKeyDecoder()

    /**
     * @throws VerificationResult.Failure
     */
    @Suppress("ThrowsCount")
    fun verifyDeviceAuth(
        document: VerifiableDocument.WithPresentation,
        sessionTranscriptBytes: ByteArray?,
        deviceKeyInfo: DeviceKeyInfo
    ) {
        verifyKeyAuthorizations(document, deviceKeyInfo)

        val root = try {
            cborMapper.readTree(document.deviceSigned.deviceSignature) as? ArrayNode
        } catch (@Suppress("TooGenericExceptionCaught") _: Exception) {
            null
        } ?: throw VerificationResult.Failure(VerificationError.INVALID_DEVICE_SIGNATURE)

        if (root.size() != COSE_SIGN1_ARRAY_SIZE) {
            throw VerificationResult.Failure(VerificationError.INVALID_DEVICE_SIGNATURE)
        }

        val payload = root[INDEX_PAYLOAD]
        if (payload != null && !payload.isNull) {
            throw VerificationResult.Failure(VerificationError.INVALID_DEVICE_SIGNATURE)
        }

        val deviceAuthBytes = buildDeviceAuthenticationBytes(
            sessionTranscriptBytes,
            document.docType,
            document.deviceSigned.deviceNameSpacesBytes
        )

        val publicKey = coseKeyDecoder.decode(deviceKeyInfo.deviceKey)

        trustVerifier.verifyCOSESign1(
            document.deviceSigned.deviceSignature,
            publicKey,
            deviceAuthBytes
        )
    }

    private fun verifyKeyAuthorizations(
        document: VerifiableDocument.WithPresentation,
        deviceKeyInfo: DeviceKeyInfo
    ) {
        val keyAuthorizations = deviceKeyInfo.keyAuthorizations ?: return
        val inner = try {
            val node = cborMapper.readTree(document.deviceSigned.deviceNameSpacesBytes)
            if (node.isBinary) {
                cborMapper.readTree((node as BinaryNode).binaryValue())
            } else {
                node
            }
        } catch (@Suppress("TooGenericExceptionCaught") _: Exception) {
            return
        }
        for (entry in inner.properties()) {
            if (entry.key !in keyAuthorizations.values &&
                entry.key !in keyAuthorizations.keys
            ) {
                throw VerificationResult.Failure(VerificationError.INVALID_DEVICE_KEY)
            }
        }
    }

    fun buildDeviceAuthenticationBytes(
        sessionTranscriptBytes: ByteArray?,
        docType: String,
        deviceNameSpacesBytes: ByteArray
    ): ByteArray {
        val innerArray = ByteArrayOutputStream().also { out ->
            out.write(CBOR_ARRAY_4)
            CBORFactory().createGenerator(out)
                .use { gen -> gen.writeString(DEVICE_AUTHENTICATION_LABEL) }
            out.write(sessionTranscriptBytes ?: byteArrayOf())
            CBORFactory().createGenerator(out).use { gen -> gen.writeString(docType) }
            out.write(deviceNameSpacesBytes)
        }.toByteArray()

        return ByteArrayOutputStream().also { out ->
            CBORFactory().createGenerator(out).use { gen ->
                gen.writeTag(TAG_24)
                gen.writeBinary(innerArray)
            }
        }.toByteArray()
    }

    private companion object {
        const val CBOR_ARRAY_4 = 0x84
        const val TAG_24 = 24
        const val DEVICE_AUTHENTICATION_LABEL = "DeviceAuthentication"
        const val COSE_SIGN1_ARRAY_SIZE = 4
        const val INDEX_PAYLOAD = 2
    }
}
