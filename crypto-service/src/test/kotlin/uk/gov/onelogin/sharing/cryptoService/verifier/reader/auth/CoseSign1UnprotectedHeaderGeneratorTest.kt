package uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth

import io.mockk.mockk
import javax.security.cert.Certificate
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.collection.IsMapContaining.hasKey
import uk.gov.logging.testdouble.v2.SystemLogger

class CoseSign1UnprotectedHeaderGeneratorTest {

    private val logger = SystemLogger()
    private val certificateOne: Certificate = mockk(relaxed = true)
    private val certificateTwo: Certificate = mockk(relaxed = true)

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

        assertTrue("Entries should all be certificates!") {
            arrayWrapper.all { it is Certificate }
        }

        val x5ChainWrapper = arrayWrapper.map { it as Certificate }.toTypedArray()

        assertContentEquals(
            x5ChainWrapper,
            arrayOf(certificateOne, certificateTwo)
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