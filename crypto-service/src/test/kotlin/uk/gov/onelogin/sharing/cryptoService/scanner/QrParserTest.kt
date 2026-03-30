package uk.gov.onelogin.sharing.cryptoService.scanner

import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.google.testing.junit.testparameterinjector.TestParameters
import kotlin.test.Test
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class QrParserTest {

    private val parser = QrParserImpl()

    @Test
    @TestParameters(valuesProvider = QrParserProvider::class)
    fun `parse returns expected result`(input: String?, expected: QrScanResult) {
        val result = parser.parse(input)

        assertEquals(expected, result)
    }
}
