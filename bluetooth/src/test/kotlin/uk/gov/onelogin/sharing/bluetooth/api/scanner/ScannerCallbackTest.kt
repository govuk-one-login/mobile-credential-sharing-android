package uk.gov.onelogin.sharing.bluetooth.api.scanner

import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import kotlin.test.Test
import kotlin.test.assertEquals
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.bluetooth.api.scanner.ScannerCallback.Companion.toLeScanCallback

@RunWith(TestParameterInjector::class)
class ScannerCallbackTest {

    @Test
    fun assertFailureMapping(@TestParameter input: ScannerFailureMapping) {
        var failure: ScannerFailure? = null

        ScannerCallback.of(onFailure = { failure = it })
            .toLeScanCallback()
            .onScanFailed(input.errorCode)

        assertEquals(
            expected = input.expectedFailure,
            actual = failure
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
