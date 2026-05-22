package uk.gov.onelogin.sharing.verification.document

import java.security.cert.X509Certificate
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.collection.IsCollectionWithSize.hasSize
import org.junit.Test
import uk.gov.onelogin.sharing.verification.ClassInfoExt.scanResult
import uk.gov.onelogin.sharing.verification.trust.TrustVerifier

class Iso18013DocumentVerifierTest {

    /**
     * DCMAW-20246: AC1: [Iso18013DocumentVerifier] compiles, implements [DocumentVerifier],
     * accepts a [X509Certificate] and a [TrustVerifier] in its constructor.
     */
    @Test
    fun `Ensure constructor constraints`() {
        val constructorInfo = scanResult.getClassInfo(Iso18013DocumentVerifier::class.java.name)
            .constructorInfo

        assertThat(
            constructorInfo,
            hasSize(1)
        )

        assertThat(
            constructorInfo[0].typeDescriptor.toStringWithSimpleNames(),
            equalTo("void (" +
                    "${X509Certificate::class.java.simpleName}, " +
                    "${TrustVerifier::class.java.simpleName})"
            )
        )
    }
}