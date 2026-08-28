package uk.gov.onelogin.sharing.verification.cose

import io.github.classgraph.ClassInfo
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import uk.gov.onelogin.sharing.verification.ClassInfoExt.scanResult

internal class CoseVerificationFailureTest {

    @Test
    fun `CoseVerificationFailure has expected inheritors`() {
        val expectedInheritors = setOf(
            "MalformedCoseSign1",
            "UnsupportedAlgorithm",
            "InvalidSignature",
            "UntrustedCertificate",
            "ExpiredCertificate",
            "UnsupportedCertificateProfile"
        )

        val classInfo = scanResult.getSubclasses(CoseVerificationFailure::class.java)

        assertThat(
            classInfo.map(ClassInfo::getSimpleName).toSet(),
            equalTo<Set<String>>(expectedInheritors)
        )
    }

    @Test
    fun `MalformedCoseSign1 is throwable`() {
        assertThrows(CoseVerificationFailure.MalformedCoseSign1::class.java) {
            throw CoseVerificationFailure.MalformedCoseSign1
        }
    }

    @Test
    fun `UnsupportedAlgorithm is throwable`() {
        assertThrows(CoseVerificationFailure.UnsupportedAlgorithm::class.java) {
            throw CoseVerificationFailure.UnsupportedAlgorithm
        }
    }

    @Test
    fun `InvalidSignature is throwable`() {
        assertThrows(CoseVerificationFailure.InvalidSignature::class.java) {
            throw CoseVerificationFailure.InvalidSignature
        }
    }

    @Test
    fun `UntrustedCertificate is throwable`() {
        assertThrows(CoseVerificationFailure.UntrustedCertificate::class.java) {
            throw CoseVerificationFailure.UntrustedCertificate
        }
    }

    @Test
    fun `ExpiredCertificate is throwable`() {
        assertThrows(CoseVerificationFailure.ExpiredCertificate::class.java) {
            throw CoseVerificationFailure.ExpiredCertificate
        }
    }

    @Test
    fun `UnsupportedCertificateProfile is throwable`() {
        assertThrows(CoseVerificationFailure.UnsupportedCertificateProfile::class.java) {
            throw CoseVerificationFailure.UnsupportedCertificateProfile(
                CertificateProfileReason.UNSUPPORTED_ALGORITHM
            )
        }
    }
}
