package uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth

import java.security.Security
import java.security.cert.Certificate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.collection.IsMapContaining.hasKey
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.verification.trust.CertificateStubs
import uk.gov.onelogin.sharing.verification.trust.TestCertificateGenerator

class CoseSign1UnprotectedHeaderGeneratorTest {

    private val logger = SystemLogger()

    private val certificateOne: Certificate = TestCertificateGenerator(
        subject = "CN=Leaf,ST=London",
        keyPair = CertificateStubs.leafKeyPair,
        issuerKeyPair = CertificateStubs.rootKeyPair,
        issuer = "CN=Intermediate,ST=London"
    ).leaf().build()

    private val certificateTwo: Certificate = TestCertificateGenerator(
        subject = "CN=Intermediate,ST=London",
        keyPair = CertificateStubs.leafKeyPair,
        issuerKeyPair = CertificateStubs.rootKeyPair,
        issuer = "CN=Issuer,ST=London"
    ).build()

    private var certificateChain = listOf(
        certificateOne,
        certificateTwo
    )

    private val generator by lazy {
        CoseSign1UnprotectedHeaderGenerator(logger)
    }

    private val resultData: Map<Long, Any> by lazy {
        generator.generateUnprotectedHeaders(certificateChain).first
    }

    private val result: ByteArray by lazy {
        generator.generateUnprotectedHeaders(certificateChain).second
    }

    @BeforeTest
    fun setUp() {
        Security.addProvider(BouncyCastleProvider())
    }

    @AfterTest
    fun tearDown() {
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
    }

    @Test
    fun `Contains an x5 chain key`() = runTest {
        assertThat(
            resultData.size,
            equalTo(1)
        )

        assertThat(
            resultData,
            hasKey(equalTo(33L))
        )
    }

    @Test
    fun `X5 chain value is an array in the same order as the provided List`() = runTest {
        assertThat(
            resultData[33],
            instanceOf(Array::class.java)
        )

        val arrayWrapper = resultData[33] as Array<*>

        assertThat(
            arrayWrapper.size,
            equalTo(2)
        )

        assertTrue("Entries should all be byte arrays!") {
            arrayWrapper.all { it is ByteArray }
        }

        val x5ChainWrapper = arrayWrapper.map { it as ByteArray }.toTypedArray()

        x5ChainWrapper.forEachIndexed { index, cborCertChain ->
            assertThat(
                "Cannot find substring for index $index's certificate content",
                result.toHexString(),
                containsString(cborCertChain.toHexString())
            )
        }
    }

    @Test
    fun `Provides a log message for header generation`() = runTest {
        assertNotNull(resultData)

        assertTrue {
            "Generated unprotected headers for COSE_Sign1 structure" in logger
        }
    }
}
