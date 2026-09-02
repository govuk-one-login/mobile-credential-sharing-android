package uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth

import java.security.KeyPairGenerator
import java.security.Security
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.collection.IsMapContaining.hasKey
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMapper
import uk.gov.onelogin.sharing.verification.trust.CertificateStubs
import uk.gov.onelogin.sharing.verification.trust.TestCertificateGenerator

class CoseSign1UnprotectedHeaderGeneratorTest {

    private val certificateFactory = CertificateFactory.getInstance("X.509")
    private val keyPairGenerator = KeyPairGenerator.getInstance("EC")

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

    private val result by lazy {
        generator.generateUnprotectedHeaders(certificateChain)
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
            result.size,
            equalTo(1)
        )

        assertThat(
            result,
            hasKey(equalTo(33U))
        )
    }

    @Test
    fun `X5 chain value is an array in the same order as the provided List`() = runTest {
        assertThat(
            result[33U],
            instanceOf(Array::class.java)
        )

        val arrayWrapper = result[33U] as Array<*>

        assertThat(
            arrayWrapper.size,
            equalTo(2)
        )

        assertTrue("Entries should all be byte arrays!") {
            arrayWrapper.all { it is ByteArray }
        }

        val x5ChainWrapper = arrayWrapper.map { it as ByteArray }.toTypedArray()

        assertContentEquals(
            x5ChainWrapper[0],
                CborMapper.default.writeValueAsBytes(certificateOne.encoded),
        )
        assertContentEquals(
            x5ChainWrapper[1],
            CborMapper.default.writeValueAsBytes(certificateTwo.encoded),
        )
    }

    @Test
    fun `Provides a log message for header generation`() = runTest {
        assertNotNull(result)

        assertTrue {
            "Generated unprotected headers for COSE_Sign1 structure" in logger
        }
    }

}