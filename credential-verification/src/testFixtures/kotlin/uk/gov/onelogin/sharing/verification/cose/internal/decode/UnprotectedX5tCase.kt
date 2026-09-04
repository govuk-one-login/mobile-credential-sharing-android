package uk.gov.onelogin.sharing.verification.cose.internal.decode

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.testing.junit.testparameterinjector.TestParameterValuesProvider
import uk.gov.onelogin.sharing.verification.cose.internal.path.CertificateStubs

data class UnprotectedX5tCase(val description: String, val applyTo: ObjectNode.() -> Unit) {
    override fun toString(): String = description
}

class MissingProtectedX5tProvider : TestParameterValuesProvider() {
    override fun provideValues(context: Context?): List<UnprotectedX5tCase> = listOf(
        UnprotectedX5tCase("x5t absent from both headers") { },
        UnprotectedX5tCase("x5t present only in unprotected header") {
            set<ArrayNode>(
                CoseSign1Builder.X5T_LABEL,
                CoseSign1Builder.sha256X5t(CertificateStubs.leaf)
            )
        }
    )
}
