package uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth

import java.nio.ByteBuffer
import java.security.MessageDigest
import java.security.cert.Certificate
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.collection.IsMapContaining.hasEntry
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.cryptoService.cryptography.Constants.HASH_ALGORITHM_SHA256
import uk.gov.onelogin.sharing.verification.trust.CertificateStubs
import uk.gov.onelogin.sharing.verification.trust.TestCertificateGenerator


class CoseSign1ProtectedHeadersTest {
    private val logger = SystemLogger()
    private val leafCertificate: Certificate = TestCertificateGenerator(
        subject = "CN=Leaf,ST=London",
        keyPair = CertificateStubs.leafKeyPair,
        issuerKeyPair = CertificateStubs.rootKeyPair,
        issuer = "CN=Intermediate,ST=London"
    ).leaf().build()

    private val generator by lazy {
        CoseSign1ProtectedHeaders(logger)
    }

    private val hashedLeafCertificate by lazy {
        MessageDigest
            .getInstance(HASH_ALGORITHM_SHA256)
            .digest(leafCertificate.encoded)
    }

    private val resultData: Map<Long, Any> by lazy {
        generator.generateUnprotectedHeaderData(leafCertificate)
    }

    private val result: ByteArray by lazy {
        generator.generateProtectedHeaders(leafCertificate)
    }

    @Test
    fun `Protected headers are in the correct structure`() = runTest {
        val initialStructureMatchers = listOf(
            hasEntry<Long, Any>(
                equalTo(1L),
                equalTo(-7)
            ),
            hasEntry(
                equalTo(34L),
                instanceOf(Array::class.java)
            )
        ).let(::allOf)

        assertThat(
            resultData,
            initialStructureMatchers
        )
    }

    @Test
    fun `X5T header value provides the hashing algorithm and hashed certificate`() = runTest {
        val dataArray = (resultData[34L] as Array<*>).toList()

        assertThat(
            dataArray,
            Matchers.contains(
                -16,
                MessageDigest.getInstance(HASH_ALGORITHM_SHA256).digest(
                    leafCertificate.encoded
                )
            )
        )

        assertTrue {
            "Generated protected headers for COSE_Sign1 structure" in logger
        }
    }

    @Test
    fun `Interface function provides CBOR encoded headers`() {
        val arrayWrapper = resultData[34L] as Array<*>

        assertThat(
            arrayWrapper.size,
            equalTo(2)
        )

       listOf(
           "Hashed leaf certificate" to hashedLeafCertificate
       ).forEach { (attribute, value) ->
           assertThat(
               "Cannot find provided data for '$attribute'",
               result.toHexString(),
               containsString(value.toHexString())
           )
       }
    }

    private fun Long.toBytes(): ByteArray {
        val buffer = ByteBuffer.allocate(java.lang.Long.BYTES)
        buffer.putLong(this)
        return buffer.array()
    }

    private fun Int.toBytes(): ByteArray {
        val buffer = ByteBuffer.allocate(Integer.BYTES)
        buffer.putInt(this)
        return buffer.array()

    }
}
