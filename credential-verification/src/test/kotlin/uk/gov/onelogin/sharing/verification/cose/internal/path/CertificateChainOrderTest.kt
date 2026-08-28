package uk.gov.onelogin.sharing.verification.cose.internal.path

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class CertificateChainOrderTest {

    @Test
    fun `reorders a shuffled certificate chain`() {
        val root = CertificateStubs.rootCa
        val intermediate = CertificateStubs.intermediateCa
        val leaf = CertificateStubs.leaf

        val shuffled = listOf(intermediate, leaf, root)
        val ordered = orderCertificates(shuffled)

        assertThat(ordered.size, equalTo(3))
        assertThat(ordered[0], equalTo(leaf))
        assertThat(ordered[1], equalTo(intermediate))
        assertThat(ordered[2], equalTo(root))
    }

    @Test
    fun `returns original list if already ordered`() {
        val intermediate = CertificateStubs.intermediateCa
        val leaf = CertificateStubs.leaf

        val chain = listOf(leaf, intermediate)
        val ordered = orderCertificates(chain)

        assertThat(ordered, equalTo(chain))
    }

    @Test
    fun `returns original list if size is 1`() {
        val leaf = listOf(CertificateStubs.leaf)
        val ordered = orderCertificates(leaf)

        assertThat(ordered, equalTo(leaf))
    }
}
