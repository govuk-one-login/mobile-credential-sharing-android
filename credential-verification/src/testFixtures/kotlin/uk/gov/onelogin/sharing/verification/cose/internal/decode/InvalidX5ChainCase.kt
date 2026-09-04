package uk.gov.onelogin.sharing.verification.cose.internal.decode

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.testing.junit.testparameterinjector.TestParameterValuesProvider
import uk.gov.onelogin.sharing.verification.cose.internal.path.CertificateStubs

/**
 * AC4: an invalid `x5chain` placed in the unprotected header. Every case must fail with
 * `MalformedCoseSign1`. [applyTo] mutates the unprotected-header [ObjectNode] in place.
 */
data class InvalidX5ChainCase(val description: String, val applyTo: ObjectNode.() -> Unit) {
    override fun toString(): String = description
}

/** Supplies the AC4 [InvalidX5ChainCase]s to `@TestParameter`. */
class InvalidX5ChainProvider : TestParameterValuesProvider() {
    override fun provideValues(context: Context?): List<InvalidX5ChainCase> = listOf(
        InvalidX5ChainCase("non-byte-string x5chain") {
            put(CoseSign1Builder.X5CHAIN_LABEL, 42)
        },
        InvalidX5ChainCase("mixed-type x5chain array") {
            val arr = arrayNode()
            arr.add(CertificateStubs.leaf.encoded)
            arr.add("not-a-cert")
            set<ArrayNode>(CoseSign1Builder.X5CHAIN_LABEL, arr)
        },
        InvalidX5ChainCase("non-DER certificate byte string") {
            put(CoseSign1Builder.X5CHAIN_LABEL, byteArrayOf(0x00, 0x01, 0x02))
        }
    )
}
