@file:Suppress("UnusedParameter")

package uk.gov.onelogin.sharing.verification.document

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BinaryNode
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import dev.zacsweers.metro.ContributesBinding
import java.io.ByteArrayOutputStream
import java.security.cert.X509Certificate
import uk.gov.onelogin.sharing.verification.CredentialVerificationScope
import uk.gov.onelogin.sharing.verification.document.cose.CoseKeyDecoder
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObject
import uk.gov.onelogin.sharing.verification.format.document.VerifiableDocument
import uk.gov.onelogin.sharing.verification.format.document.device.DeviceKeyInfo
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult
import uk.gov.onelogin.sharing.verification.format.document.validity.CertificateValidityPeriod
import uk.gov.onelogin.sharing.verification.trust.TrustVerifier

@ContributesBinding(CredentialVerificationScope::class)
class Iso18013DocumentVerifier(
    private val trustedRootCertificate: X509Certificate,
    private val trustVerifier: TrustVerifier
) : DocumentVerifier {
    private val cborMapper = ObjectMapper(CBORFactory())
    private val coseKeyDecoder = CoseKeyDecoder()

    override fun verifyDocument(
        document: VerifiableDocument,
        sessionTranscriptBytes: ByteArray?
    ): VerificationResult.Success {
        val issuerAuthResult = trustVerifier.verifyCOSESign1(
            document.issuerSigned.issuerAuth,
            trustedRootCertificate
        )
        val mso = decodeMSO(issuerAuthResult.msoPayload)

        verifyMSOFields(document, mso)
        verifyDocumentDigests(document, mso)
        verifyValidityInfo(issuerAuthResult.certificateValidityPeriod, mso)

        if (document is VerifiableDocument.WithPresentation) {
            if (sessionTranscriptBytes == null) {
                throw VerificationResult.Failure(VerificationError.INVALID_DEVICE_SIGNATURE)
            }

            verifyDeviceAuth(document, sessionTranscriptBytes, mso.deviceKeyInfo)
        }

        return VerificationResult.Success
    }

    /**
     * @throws VerificationResult.Failure
     */
    internal fun decodeMSO(encodedMSO: ByteArray): MobileSecurityObject =
        throw VerificationResult.Failure(VerificationError.MALFORMED_MSO)

    /**
     * @throws VerificationResult.Failure
     */
    internal fun verifyMSOFields(document: VerifiableDocument, mso: MobileSecurityObject): Unit =
        throw VerificationResult.Failure(VerificationError.INVALID_MSO_VERSION)

    /**
     * @throws VerificationResult.Failure
     */
    internal fun verifyDocumentDigests(
        document: VerifiableDocument,
        mso: MobileSecurityObject
    ): Unit = throw VerificationResult.Failure(VerificationError.DIGEST_MISMATCH)

    /**
     * @throws VerificationResult.Failure
     */
    internal fun verifyValidityInfo(
        validityPeriod: CertificateValidityPeriod,
        mso: MobileSecurityObject
    ): Unit = throw VerificationResult.Failure(VerificationError.VALIDITY_SIGNED_OUT_OF_RANGE)

    /**
     * @throws VerificationResult.Failure
     */
    @Suppress("ThrowsCount", "CyclomaticComplexMethod")
    internal fun verifyDeviceAuth(
        document: VerifiableDocument.WithPresentation,
        sessionTranscriptBytes: ByteArray?,
        deviceKeyInfo: DeviceKeyInfo
    ) {
        val keyAuthorizations = deviceKeyInfo.keyAuthorizations
        if (keyAuthorizations != null) {
            val inner = try {
                val node = cborMapper.readTree(document.deviceSigned.deviceNameSpacesBytes)
                if (node.isBinary) {
                    cborMapper.readTree((node as BinaryNode).binaryValue())
                } else {
                    node
                }
            } catch (@Suppress("TooGenericExceptionCaught") _: Exception) {
                null
            }
            if (inner != null) {
                for (entry in inner.properties()) {
                    if (entry.key !in keyAuthorizations.values &&
                        entry.key !in keyAuthorizations.keys
                    ) {
                        throw VerificationResult.Failure(VerificationError.INVALID_DEVICE_KEY)
                    }
                }
            }
        }

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

    internal fun buildDeviceAuthenticationBytes(
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
