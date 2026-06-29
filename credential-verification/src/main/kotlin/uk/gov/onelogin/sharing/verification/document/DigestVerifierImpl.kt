package uk.gov.onelogin.sharing.verification.document

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.BinaryNode
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import java.security.MessageDigest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponseDto
import uk.gov.onelogin.sharing.verification.CredentialVerificationScope
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObject
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObject.Companion.MSO_DIGEST_ALGORITHM
import uk.gov.onelogin.sharing.verification.format.document.VerifiableDocument
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult

@Inject
@ContributesBinding(
    scope = CredentialVerificationScope::class,
    binding = binding<DigestVerifier>()
)
class DigestVerifierImpl : DigestVerifier {

    private val cborMapper = ObjectMapper(CBORFactory())

    override fun verify(document: VerifiableDocument, mso: MobileSecurityObject) {
        val nameSpaces = document.issuerSigned.nameSpaces ?: return

        nameSpaces.forEach { (namespace, items) ->
            val msoDigests = mso.valueDigests[namespace]
            items.forEach { itemBytes ->
                val digestId = extractDigestId(itemBytes)
                val expectedDigest = msoDigests?.get(digestId)
                    ?: throw VerificationResult.Failure(VerificationError.DIGEST_MISSING)
                val computedDigest = MessageDigest.getInstance(
                    MSO_DIGEST_ALGORITHM
                ).digest(itemBytes)
                if (!MessageDigest.isEqual(computedDigest, expectedDigest)) {
                    throw VerificationResult.Failure(VerificationError.DIGEST_MISMATCH)
                }
            }
        }
    }

    private fun extractDigestId(itemBytes: ByteArray): Int {
        val innerBytes = unwrapTag24(itemBytes)
        val item = cborMapper.readValue(
            innerBytes,
            DeviceResponseDto.IssuerSignedItemDTO::class.java
        )
        val id = item.digestId.toUInt()
        if (id >= MAX_DIGEST_ID) {
            throw VerificationResult.Failure(VerificationError.DIGEST_MISMATCH)
        }
        return id.toInt()
    }

    private fun unwrapTag24(data: ByteArray): ByteArray {
        val root = cborMapper.readTree(data)
        return (root as? BinaryNode)?.binaryValue()
            ?: throw VerificationResult.Failure(VerificationError.DIGEST_MISMATCH)
    }

    private companion object {
        const val MAX_DIGEST_ID = 2_147_483_648u // 2^31
    }
}
