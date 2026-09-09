package uk.gov.onelogin.sharing.verification.cose.internal.path

import org.junit.Test

class IacaContentCheckerTest {
    private val validator = CertificateChainValidatorImpl()

    @Test
    fun `leaf with critical EKU passes`() {
        validator.verify(
            listOf(CertificateStubs.leafSignedByRoot),
            CertificateStubs.rootCa
        )
    }
}
