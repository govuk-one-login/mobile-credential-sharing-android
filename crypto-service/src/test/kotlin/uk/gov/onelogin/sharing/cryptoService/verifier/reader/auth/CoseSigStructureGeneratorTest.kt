package uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth

import com.fasterxml.jackson.dataformat.cbor.CBORConstants
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.namedTestValuesIn
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import java.security.cert.Certificate
import kotlin.test.Test
import kotlin.test.assertTrue
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.arrayContaining
import org.hamcrest.Matchers.startsWith
import org.hamcrest.Matchers.stringContainsInOrder
import org.junit.runner.RunWith
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.models.mdoc.cbor.HexFormatter
import uk.gov.onelogin.sharing.verification.trust.CertificateStubs
import uk.gov.onelogin.sharing.verification.trust.TestCertificateGenerator

@RunWith(TestParameterInjector::class)
class CoseSigStructureGeneratorTest {

    private val logger = SystemLogger()
    private var protectedHeaders: ByteArray = byteArrayOf(1, 2, 3)
    private var readerAuthenticationPayload: ByteArray = byteArrayOf(2, 3, 4)

    private val certificateGenerator = TestCertificateGenerator(
        subject = "CN=Unit Test Subject,ST=London",
        keyPair = CertificateStubs.leafKeyPair,
        issuerKeyPair = CertificateStubs.rootKeyPair,
        issuer = "CN=Intermediate,ST=London"
    )

    private val certificateChain: List<Certificate> by lazy {
        listOf(
            certificateGenerator.leaf().build(),
            certificateGenerator.build()
        )
    }

    private val protectedHeaderGenerator by lazy {
        ProtectedHeaderGenerator {
            protectedHeaders
        }
    }

    private val generator by lazy {
        CoseSigStructureGenerator(
            logger = logger,
            protectedHeaderGenerator = protectedHeaderGenerator
        )
    }

    private val resultData by lazy {
        generator.generateSignatureStructureData(
            certificateChain,
            readerAuthenticationPayload
        ).also {
            assertTrue {
                "Generated Sig_Structure with protected headers and reader auth bytes" in logger
            }
        }
    }

    private val result by lazy {
        generator.generateSignatureStructure(
            certificateChain,
            readerAuthenticationPayload
        ).also {
            assertTrue {
                "CBOR-encoded Sig_Structure array: ${it.toHexString()}" in logger
            }
        }
    }

    private val resultHexString by lazy {
        result.toHexString()
    }

    @Test
    fun `Internal data retrieval meets structural expectations`() {
        assertThat(
            resultData,
            arrayContaining(
                "Signature1",
                protectedHeaders,
                "",
                readerAuthenticationPayload
            )
        )
    }

    @Test
    fun `CBOR generation meets structural expectations`(
        @TestParameter assertion: Matcher<in String> = namedTestValuesIn(
            mapOf(
                "Begins with a 4-element array" to startsWith(
                    HexFormatter(CBORConstants.PREFIX_TYPE_ARRAY + 4)
                ),
                "Contains the correct substring order" to stringContainsInOrder(
                    resultData.map {
                        when (it) {
                            is ByteArray -> it.toHexString()
                            is String -> it.toByteArray().toHexString()
                            else -> throw Exception("Unexpected data type in data array")
                        }
                    }
                )
            )
        )
    ) {
        assertThat(
            resultHexString,
            assertion
        )
    }
}
