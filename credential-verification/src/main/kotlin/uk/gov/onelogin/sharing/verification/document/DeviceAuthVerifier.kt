package uk.gov.onelogin.sharing.verification.document

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BinaryNode
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import dev.zacsweers.metro.Inject
import uk.gov.onelogin.sharing.verification.document.cose.CoseKeyDecoder
import uk.gov.onelogin.sharing.verification.format.document.VerifiableDocument
import uk.gov.onelogin.sharing.verification.format.document.device.DeviceKeyInfo
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult
import uk.gov.onelogin.sharing.verification.trust.TrustVerifier

@Inject
class DeviceAuthVerifier(
    private val trustVerifier: TrustVerifier,
    private val coseKeyDecoder: CoseKeyDecoder,
    private val deviceAuthenticationEncoder: DeviceAuthenticationEncoder
) {
    private val cborMapper = ObjectMapper(CBORFactory())

    /**
     * @throws VerificationResult.Failure
     */
    @Suppress("ThrowsCount")
    fun verify(
        document: VerifiableDocument.WithPresentation,
        sessionTranscriptBytes: ByteArray,
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

        val deviceAuthBytes = deviceAuthenticationEncoder.encode(
            sessionTranscriptBytes = sessionTranscriptBytes,
            docType = document.docType,
            deviceNameSpacesBytes = document.deviceSigned.deviceNameSpacesBytes
        )

        val publicKey = coseKeyDecoder.decode(deviceKeyInfo.deviceKey)

        trustVerifier.verifyCOSESign1(
            coseData = document.deviceSigned.deviceSignature,
            publicKey = publicKey,
            payload = deviceAuthBytes
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
            throw VerificationResult.Failure(VerificationError.INVALID_DEVICE_KEY)
        }
        for (entry in inner.properties()) {
            if (entry.key !in keyAuthorizations.values &&
                entry.key !in keyAuthorizations.keys
            ) {
                throw VerificationResult.Failure(VerificationError.INVALID_DEVICE_KEY)
            }
        }
    }

    private companion object {
        const val COSE_SIGN1_ARRAY_SIZE = 4
        const val INDEX_PAYLOAD = 2
    }
}
