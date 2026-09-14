package uk.gov.onelogin.sharing.verification.cose

import uk.gov.onelogin.sharing.verification.cose.internal.decode.CoseSign1Builder
import uk.gov.onelogin.sharing.verification.cose.internal.path.CertificateStubs

object CoseVectors {

    val attachedMsoPayloadBytes = byteArrayOf(0x01)

    val attachedMsoBytes: ByteArray
        get() = createAttachedVector()

    fun createAttachedVector(
        includeX5chain: Boolean = true,
        alg: Long = -7L,
        eku: String = "1.0.18013.5.1.2",
        tamperSignature: Boolean = false
    ): ByteArray {
        val leaf = if (eku != "1.0.18013.5.1.2") {
            uk.gov.onelogin.sharing.verification.cose.internal.path.TestCertificateGenerator(
                subject = "CN=Leaf,C=GB,ST=London",
                keyPair = CertificateStubs.leafKeyPair,
                issuerKeyPair = CertificateStubs.rootKeyPair,
                issuer = "CN=Root,C=GB,ST=London"
            ).leaf().withEkuOids(listOf(eku)).build()
        } else {
            CertificateStubs.leafSignedByRoot
        }

        val protectedHeader = CoseSign1Builder.protectedHeaderBytes(listOf(leaf)) {
            put("1", alg)
        }

        val unprotectedNode = CoseSign1Builder.objectNode()
        if (includeX5chain) {
            val chainNode = CoseSign1Builder.objectNode().arrayNode()
                .add(CoseSign1Builder.objectNode().binaryNode(leaf.encoded))
            unprotectedNode.set<com.fasterxml.jackson.databind.node.ArrayNode>(
                CoseSign1Builder.X5CHAIN_LABEL,
                chainNode
            )
        }
        val unprotectedHeader = CoseSign1Builder.toBytes(unprotectedNode)
        val signatureBytes = if (tamperSignature) {
            ByteArray(64) { 0x01 }
        } else {
            CoseSign1Builder.sign(
                protectedHeader,
                attachedMsoPayloadBytes,
                CertificateStubs.leafKeyPair
            )
        }

        return CoseSign1Builder.assembleUnsigned(
            protectedHeader,
            unprotectedHeader,
            attachedMsoPayloadBytes,
            signatureBytes
        )
    }
}
