package uk.gov.onelogin.sharing.bluetooth.api.scanner

import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import kotlin.test.Test
import kotlin.test.assertEquals
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class ScannerFailureTest {

    @Test
    fun assertFailureMapping(@TestParameter input: ScannerFailureMapping) {
        assertEquals(
            expected = input.expectedFailure,
            actual = ScannerFailure.from(input.errorCode)
        )
    }

    @Test
    fun mappingsMatchEnumEntrySize() {
        assertEquals(
            ScannerFailureMapping.entries.size,
            ScannerFailure.entries.size
        )
    }
}
