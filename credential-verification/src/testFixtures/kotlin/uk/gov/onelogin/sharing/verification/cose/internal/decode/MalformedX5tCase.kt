package uk.gov.onelogin.sharing.verification.cose.internal.decode

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.testing.junit.testparameterinjector.TestParameterValuesProvider
import java.security.MessageDigest
import uk.gov.onelogin.sharing.verification.cose.internal.path.CertificateStubs
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObject.Companion.MSO_DIGEST_ALGORITHM

/**
 * AC6: a malformed `x5t` installed in the protected header (with the default `x5t` suppressed).
 * Every case must fail with `MalformedCoseSign1`. [applyTo] mutates the protected-header
 * [ObjectNode] in place.
 */
data class MalformedX5tCase(val description: String, val applyTo: ObjectNode.() -> Unit) {
    override fun toString(): String = description
}

/** Supplies the AC6 [MalformedX5tCase]s to `@TestParameter`. */
class MalformedX5tProvider : TestParameterValuesProvider() {
    override fun provideValues(context: Context?): List<MalformedX5tCase> = listOf(
        MalformedX5tCase("x5t not a two-element array") {
            val arr = arrayNode()
            arr.add(CoseSign1Builder.COSE_ALG_SHA_256)
            set<ArrayNode>(CoseSign1Builder.X5T_LABEL, arr)
        },
        MalformedX5tCase("x5t first element not an algorithm identifier") {
            val digest = MessageDigest.getInstance(MSO_DIGEST_ALGORITHM)
                .digest(CertificateStubs.leaf.encoded)
            val arr = arrayNode()
            arr.add(MSO_DIGEST_ALGORITHM)
            arr.add(digest)
            set<ArrayNode>(CoseSign1Builder.X5T_LABEL, arr)
        },
        MalformedX5tCase("x5t second element not a byte string") {
            val arr = arrayNode()
            arr.add(CoseSign1Builder.COSE_ALG_SHA_256)
            arr.add("not-bytes")
            set<ArrayNode>(CoseSign1Builder.X5T_LABEL, arr)
        },
        MalformedX5tCase("SHA-256 x5t with non-32-byte hash") {
            set<ArrayNode>(
                CoseSign1Builder.X5T_LABEL,
                CoseSign1Builder.x5tNode(CoseSign1Builder.COSE_ALG_SHA_256, ByteArray(16))
            )
        }
    )
}
