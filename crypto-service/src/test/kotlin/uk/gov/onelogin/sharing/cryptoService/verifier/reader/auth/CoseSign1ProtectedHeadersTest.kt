package uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth

import io.mockk.every
import io.mockk.mockk
import java.security.MessageDigest
import java.security.cert.Certificate
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.collection.IsMapContaining.hasEntry
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.cryptoService.cryptography.Constants.HASH_ALGORITHM_SHA256

class CoseSign1ProtectedHeadersTest {
    private val logger = SystemLogger()
    private val leafCertificate: Certificate = mockk(relaxed = true)

    private val generator by lazy {
        CoseSign1ProtectedHeaders(logger)
    }

    @Test
    fun `Generates protected headers for COSE_Sign1 structure`() = runTest {
        val leafCertificateBytes = byteArrayOf(1, 2, 3, 4, 5)
        every {
            leafCertificate.encoded
        } returns leafCertificateBytes
        val result = generator.generateProtectedHeaders(leafCertificate)

        val initialStructureMatchers = listOf(
            hasEntry<UInt, Any>(
                equalTo(1U),
                equalTo(-7)
            ),
            hasEntry(
                equalTo(34U),
                instanceOf(Array::class.java)
            )
        ).let(::allOf)

        assertThat(
            result,
            initialStructureMatchers
        )

        val dataArray = (result[34U] as Array<*>).toList()

        assertThat(
            dataArray,
            Matchers.contains(
                -16,
                MessageDigest.getInstance(HASH_ALGORITHM_SHA256).digest(
                    leafCertificateBytes
                )
            )
        )

        assertTrue {
            "Generated protected headers for COSE_Sign1 structure" in logger
        }
    }
}
