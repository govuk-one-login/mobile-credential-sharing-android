package uk.gov.onelogin.sharing.cameraService.scan

import com.google.testing.junit.testparameterinjector.TestParameters
import gov.onelogin.sharing.cameraservice.callbacks.QrScannerCallbackProvider
import gov.onelogin.sharing.cameraservice.data.BarcodeDataResultStubs.invalidBarcodeDataResultOne
import gov.onelogin.sharing.cameraservice.data.BarcodeDataResultStubs.invalidBarcodeDataResultTwo
import gov.onelogin.sharing.cameraservice.data.BarcodeDataResultStubs.validBarcodeDataResult
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog
import uk.gov.android.ui.componentsv2.camera.analyzer.qr.BarcodeSourceStub.Companion.asUrlBarcodes
import uk.gov.android.ui.componentsv2.camera.qr.BarcodeScanResult
import uk.gov.onelogin.sharing.cameraService.rules.ShadowLogFile

@RunWith(RobolectricTestParameterInjector::class)
@Config(
    shadows = [ShadowLog::class]
)
class QrScannerCallbackTest {

    @get:Rule
    val loggingFile = ShadowLogFile(fileName = this::class.java.simpleName)

    private var hasToggledScanner = false
    private var scanData: String? = null

    private val callback = QrScannerCallback {
        scanData = it
    }

    @Test
    fun onlyChecksTheFirstBarcode() = performLoggingFlow(
        result = BarcodeScanResult.Success(
            listOf(
                invalidBarcodeDataResultOne,
                validBarcodeDataResult
            ).asUrlBarcodes()
        ),
        expectedData = invalidBarcodeDataResultOne
    ).also {
        assert(
            loggingFile.none {
                invalidBarcodeDataResultTwo in it
            }
        )
    }

    @TestParameters(valuesProvider = QrScannerCallbackProvider::class)
    @Test
    fun performLoggingFlow(result: BarcodeScanResult, expectedData: String?) = runTest {
        callback.onResult(result) { hasToggledScanner = true }

        assert(
            loggingFile.any {
                it.contains("Obtained BarcodeScanResult")
            }
        )

        assertEquals(expectedData, scanData)
    }
}
