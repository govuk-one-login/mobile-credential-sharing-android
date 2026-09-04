package uk.gov.onelogin.sharing.verification.cose.internal.decode

import com.fasterxml.jackson.core.StreamReadFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BinaryNode
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import dev.zacsweers.metro.Inject
import java.io.ByteArrayInputStream
import java.security.MessageDigest
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import uk.gov.onelogin.sharing.verification.cose.CoseVerificationFailure
import uk.gov.onelogin.sharing.verification.cose.CoseVerificationFailure.InvalidSignature
import uk.gov.onelogin.sharing.verification.cose.CoseVerificationFailure.MalformedCoseSign1
import uk.gov.onelogin.sharing.verification.cose.CoseVerificationFailure.MissingX5Chain
import uk.gov.onelogin.sharing.verification.cose.CoseVerificationFailure.UnsupportedAlgorithm

/**
 * C4: Enforces the shared certificate-header profile used by both IssuerAuth and ReaderAuth.
 *
 * Given the decoded [InternalCoseSign1] headers from C2 (without re-encoding them), this stage:
 *  - ignores `x5bag` wherever it occurs (never chain material, never a fallback for `x5chain`),
 *  - requires a non-empty `x5chain` (label 33) present only in the unprotected header, either a
 *    single DER certificate byte string or a non-empty array of DER certificate byte strings,
 *  - requires a protected `x5t` (label 34) of the form `[SHA-256 (-16), 32-byte hash]` that equals
 *    the SHA-256 digest of the exact DER bytes of the first supplied certificate.
 *
 * It selects the first supplied certificate as the candidate leaf and preserves the complete
 * supplied sequence unchanged. It performs no certificate-path validation, no X.509 profile
 * enforcement, and no COSE signature verification.
 *
 * Any failure is raised as a typed [CoseVerificationFailure]
 * before path or signature verification is reached.
 */
@Suppress("TooManyFunctions")
@Inject
internal class CertificateHeaderValidator {

    private val cborMapper: ObjectMapper = JsonMapper.builder(CBORFactory())
        .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
        .build()

    /**
     * Enforces the certificate-header profile and binds the candidate signing leaf.
     *
     * @throws MissingX5Chain when no `x5chain` is present in either header.
     * @throws MalformedCoseSign1 for prohibited placement, malformed shapes, non-DER certificate
     *  bytes, or a missing/misplaced/wrong-length/wrong-shape `x5t`.
     * @throws UnsupportedAlgorithm when the `x5t` hash algorithm is not SHA-256 (-16).
     * @throws InvalidSignature when the `x5t` thumbprint does not match the first certificate.
     */
    fun validate(coseSign1: InternalCoseSign1): CertificateHeaderProfile {
        val decodedProtected = decodeHeader(coseSign1.protectedHeader)
        val decodedUnprotected = coseSign1.unprotectedHeader?.let { decodeHeader(it) }

        val chain = resolveChain(decodedProtected, decodedUnprotected)
        val candidateLeaf = chain.first()

        enforceThumbprint(decodedProtected, decodedUnprotected, candidateLeaf)

        return CertificateHeaderProfile(candidateLeaf = candidateLeaf, chain = chain)
    }

    private fun resolveChain(
        decodedProtected: JsonNode,
        decodedUnprotected: JsonNode?
    ): List<ByteArray> {
        if (hasLabel(decodedProtected, X5CHAIN_LABEL)) throw MalformedCoseSign1

        val x5chainNode = entry(decodedUnprotected, X5CHAIN_LABEL) ?: throw MissingX5Chain
        val chain = readCertificateBytes(x5chainNode)
        chain.forEach { requireValidDer(it) }
        return chain
    }

    private fun readCertificateBytes(node: JsonNode): List<ByteArray> = when {
        node is BinaryNode -> listOf(node.binaryValue())

        node is ArrayNode -> {
            if (node.isEmpty) throw MalformedCoseSign1
            node.map { element ->
                (element as? BinaryNode)?.binaryValue() ?: throw MalformedCoseSign1
            }
        }

        else -> throw MalformedCoseSign1
    }

    private fun requireValidDer(certBytes: ByteArray) {
        try {
            CertificateFactory.getInstance("X.509")
                .generateCertificate(ByteArrayInputStream(certBytes))
        } catch (@Suppress("SwallowedException") _: CertificateException) {
            throw MalformedCoseSign1
        }
    }

    private fun enforceThumbprint(
        decodedProtected: JsonNode,
        decodedUnprotected: JsonNode?,
        candidateLeaf: ByteArray
    ) {
        val x5tNode = resolveProtectedX5t(decodedProtected, decodedUnprotected)
        val suppliedHash = readSha256Thumbprint(x5tNode)

        val computedHash = MessageDigest.getInstance(MSO_DIGEST_ALGORITHM).digest(candidateLeaf)
        if (!MessageDigest.isEqual(computedHash, suppliedHash)) throw InvalidSignature
    }

    private fun resolveProtectedX5t(
        decodedProtected: JsonNode,
        decodedUnprotected: JsonNode?
    ): JsonNode {
        val misplaced = decodedUnprotected != null && hasLabel(decodedUnprotected, X5T_LABEL)
        if (misplaced) throw MalformedCoseSign1
        return entry(decodedProtected, X5T_LABEL) ?: throw MalformedCoseSign1
    }

    private fun readSha256Thumbprint(x5tNode: JsonNode): ByteArray {
        requireX5tShape(x5tNode)

        val algNode = x5tNode.get(0)
        val hashNode = x5tNode.get(1)
        val suppliedHash = (hashNode as BinaryNode).binaryValue()

        if (algNode.longValue() != COSE_ALG_SHA_256) throw UnsupportedAlgorithm
        if (suppliedHash.size != SHA_256_LENGTH) throw MalformedCoseSign1
        return suppliedHash
    }

    private fun requireX5tShape(x5tNode: JsonNode) {
        val valid = x5tNode is ArrayNode &&
            x5tNode.size() == X5T_ELEMENT_COUNT &&
            x5tNode.get(0).isIntegralNumber &&
            x5tNode.get(1) is BinaryNode
        if (!valid) throw MalformedCoseSign1
    }

    private fun decodeHeader(headerBytes: ByteArray): JsonNode = try {
        cborMapper.readTree(headerBytes)
    } catch (@Suppress("TooGenericExceptionCaught", "SwallowedException") _: Exception) {
        throw MalformedCoseSign1
    } ?: throw MalformedCoseSign1

    private fun hasLabel(node: JsonNode, label: String): Boolean = node.get(label) != null
    private fun entry(node: JsonNode?, label: String): JsonNode? = node?.get(label)

    private companion object {
        const val X5CHAIN_LABEL = "33"
        const val X5T_LABEL = "34"
        const val COSE_ALG_SHA_256 = -16L
        const val SHA_256_LENGTH = 32
        const val X5T_ELEMENT_COUNT = 2
        const val MSO_DIGEST_ALGORITHM = "SHA-256"
    }
}
