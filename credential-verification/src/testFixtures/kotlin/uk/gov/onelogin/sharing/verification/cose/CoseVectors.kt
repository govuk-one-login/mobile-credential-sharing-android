package uk.gov.onelogin.sharing.verification.cose

import uk.gov.onelogin.sharing.verification.cose.internal.decode.CoseSign1Builder
import uk.gov.onelogin.sharing.verification.cose.internal.path.CertificateStubs
import uk.gov.onelogin.sharing.verification.cose.internal.path.TestCertificateGenerator

private const val OID_READER_AUTH_EKU = "1.0.18013.5.1.6"
private const val OID_ISSUER_AUTH_EKU = "1.0.18013.5.1.2"

object CoseVectors {

    val attachedMsoPayloadBytes = byteArrayOf(0x01)

    val attachedMsoBytes: ByteArray
        get() = createAttachedVector()

    val detachedReaderAuthPayloadBytes = byteArrayOf(0x02, 0x03)

    val readerLeafSignedByRoot by lazy {
        TestCertificateGenerator(
            subject = "CN=Reader,C=GB,ST=London",
            keyPair = CertificateStubs.leafKeyPair,
            issuerKeyPair = CertificateStubs.rootKeyPair,
            issuer = "CN=Root,C=GB,ST=London"
        ).leaf().withEkuOids(listOf(OID_READER_AUTH_EKU)).build()
    }

    fun createAttachedVector(
        includeX5chain: Boolean = true,
        alg: Long = -7L,
        eku: String = OID_ISSUER_AUTH_EKU,
        tamperSignature: Boolean = false
    ): ByteArray {
        val leaf = if (eku != OID_ISSUER_AUTH_EKU) {
            TestCertificateGenerator(
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

    fun createDetachedVector(
        includeX5chain: Boolean = true,
        alg: Long = -7L,
        eku: String = OID_READER_AUTH_EKU,
        tamperSignature: Boolean = false,
        payload: ByteArray = detachedReaderAuthPayloadBytes
    ): ByteArray {
        val leaf = when (eku) {
            OID_READER_AUTH_EKU -> readerLeafSignedByRoot
            else -> TestCertificateGenerator(
                subject = "CN=Reader,C=GB,ST=London",
                keyPair = CertificateStubs.leafKeyPair,
                issuerKeyPair = CertificateStubs.rootKeyPair,
                issuer = "CN=Root,C=GB,ST=London"
            ).leaf().withEkuOids(listOf(eku)).build()
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
            CoseSign1Builder.sign(protectedHeader, payload, CertificateStubs.leafKeyPair)
        }

        return CoseSign1Builder.assembleUnsigned(
            protectedHeader,
            unprotectedHeader,
            payload = null,
            signatureBytes
        )
    }
}
