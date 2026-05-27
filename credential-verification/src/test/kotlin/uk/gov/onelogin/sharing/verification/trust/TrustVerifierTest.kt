package uk.gov.onelogin.sharing.verification.trust

import java.security.cert.X509Certificate
import java.security.interfaces.ECPublicKey
import kotlin.time.ExperimentalTime
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import uk.gov.onelogin.sharing.verification.ClassInfoExt.scanResult
import uk.gov.onelogin.sharing.verification.format.document.validity.CertificateValidityPeriod
import uk.gov.onelogin.sharing.verification.trust.TrustVerifier

@OptIn(ExperimentalTime::class)
class TrustVerifierTest {
    /**
     * DCMAW-20246: AC2: [TrustVerifier] exposes 2 [TrustVerifier.verifyCOSESign1] methods with the
     * correct signatures - the first method (attached payload) returning
     * a [Pair] of [CertificateValidityPeriod] and [ByteArray], with the second method
     * (detached payload) returning nothing.
     */
    @Test
    fun `Ensure TrustVerifier constraints`() {
        val methodInfo = scanResult.getClassInfo(TrustVerifier::class.java.name)
            .methodInfo

        val expectedDescriptors = listOf(
            "${Pair::class.java.name} " +
                "(byte[], ${X509Certificate::class.java.simpleName})",
            "void (byte[], ${ECPublicKey::class.java.simpleName}, byte[])"
        )

        assertThat(
            methodInfo.map { it.name },
            equalTo(listOf("verifyCOSESign1", "verifyCOSESign1"))
        )

        assertThat(
            methodInfo.map { it.typeDescriptor.toStringWithSimpleNames() },
            equalTo(expectedDescriptors)
        )
    }
}
