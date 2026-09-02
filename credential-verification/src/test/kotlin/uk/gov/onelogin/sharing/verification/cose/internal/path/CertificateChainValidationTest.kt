package uk.gov.onelogin.sharing.verification.cose.internal.path

import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.google.testing.junit.testparameterinjector.TestParameterValuesProvider
import java.security.cert.X509Certificate
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.verification.cose.CoseVerificationFailure

@RunWith(TestParameterInjector::class)
class CertificateChainValidationTest {
    private val validator = CertificateChainValidatorImpl()

    @Test
    fun `valid chain with root-intermediate-leaf validates successfully`() {
        validator.verify(
            listOf(CertificateStubs.leaf, CertificateStubs.intermediateCa),
            CertificateStubs.rootCa
        )
    }

    @Test
    fun `valid chain with root-leaf validates successfully`() {
        validator.verify(
            listOf(CertificateStubs.leafSignedByRoot),
            CertificateStubs.rootCa
        )
    }

    @Test
    fun `invalid chain throws UNTRUSTED_CERTIFICATE`(
        @TestParameter(valuesProvider = InvalidChainProvider::class) case: InvalidChainCase
    ) {
        assertThrows(CoseVerificationFailure.UntrustedCertificate::class.java) {
            validator.verify(case.chain, case.root)
        }
    }

    data class InvalidChainCase(
        val description: String,
        val chain: List<X509Certificate>,
        val root: X509Certificate
    ) {
        override fun toString(): String = description
    }

    class InvalidChainProvider : TestParameterValuesProvider() {
        override fun provideValues(context: Context?): List<InvalidChainCase> = listOf(
            InvalidChainCase(
                description = "chain not anchored to provided root",
                chain = listOf(CertificateStubs.leafSignedByUntrusted),
                root = CertificateStubs.rootCa
            ),
            InvalidChainCase(
                description = "expired leaf certificate",
                chain = listOf(CertificateStubs.expiredLeaf),
                root = CertificateStubs.rootCa
            ),
            InvalidChainCase(
                description = "not-yet-valid leaf certificate",
                chain = listOf(CertificateStubs.notYetValidLeaf),
                root = CertificateStubs.rootCa
            ),
            InvalidChainCase(
                description = "intermediate signed by wrong key",
                chain = listOf(
                    CertificateStubs.leaf,
                    CertificateStubs.intermediateSignedByWrongKey
                ),
                root = CertificateStubs.rootCa
            ),
            InvalidChainCase(
                description = "AKI-SKI mismatch",
                chain = listOf(CertificateStubs.leafWithWrongAki),
                root = CertificateStubs.rootCa
            ),
            InvalidChainCase(
                description = "intermediate without CA flag",
                chain = listOf(CertificateStubs.leaf, CertificateStubs.intermediateAsLeaf),
                root = CertificateStubs.rootCa
            ),
            InvalidChainCase(
                description = "pathLenConstraint violation",
                chain = listOf(
                    CertificateStubs.leafSignedByInter2,
                    CertificateStubs.inter2Ca,
                    CertificateStubs.intermediateWithPathLen0
                ),
                root = CertificateStubs.rootCa
            )
        )
    }
}
