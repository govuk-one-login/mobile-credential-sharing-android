package uk.gov.onelogin.sharing.cameraService.scan

import com.google.testing.junit.testparameterinjector.TestParameters
import gov.onelogin.sharing.cameraservice.callbacks.QrScannerCallbackProvider
import gov.onelogin.sharing.cameraservice.data.BarcodeDataResultStubs.invalidBarcodeDataResultOne
import gov.onelogin.sharing.cameraservice.data.BarcodeDataResultStubs.invalidBarcodeDataResultTwo
import gov.onelogin.sharing.cameraservice.data.BarcodeDataResultStubs.validBarcodeDataResult
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog
import uk.gov.android.ui.componentsv2.camera.analyzer.qr.BarcodeSourceStub.Companion.asUrlBarcodes
import uk.gov.android.ui.componentsv2.camera.qr.BarcodeScanResult
import uk.gov.onelogin.sharing.cameraService.data.BarcodeDataResult
import uk.gov.onelogin.sharing.cameraService.rules.ShadowLogFile

@RunWith(RobolectricTestParameterInjector::class)
@Config(
    shadows = [ShadowLog::class]
)
class QrScannerCallbackTest {
    @get:Rule
    val loggingFile = ShadowLogFile(fileName = this::class.java.simpleName)

    private var scanData: BarcodeDataResult? = null
    private var hasToggledScanner = false

    private val callback = QrScannerCallback {
        scanData = it
    }

    @Test
    fun onlyChecksTheFirstBarcode() = performLoggingFlow(
        result =
            BarcodeScanResult.Success(
                listOf(
                    invalidBarcodeDataResultOne.data,
                    validBarcodeDataResult.data
                ).asUrlBarcodes()
            ),
        expectedData = invalidBarcodeDataResultOne
    ).also {
        assert(
            loggingFile.none {
                invalidBarcodeDataResultTwo.data in it
            }
        )
    }

    @Test
    @TestParameters(valuesProvider = QrScannerCallbackProvider::class)
    fun performLoggingFlow(result: BarcodeScanResult, expectedData: BarcodeDataResult) = runTest {
        callback.onResult(result) { hasToggledScanner = true }

        assert(
            loggingFile.any {
                it.startsWith(
                    "I/QrScannerCallback: Obtained BarcodeScanResult: ${result::class.java.simpleName}"
                )
            }
        ) {
            "Couldn't find a \"${result::class.java.simpleName}\" entry in: " +
                loggingFile.readLines()
        }

        assertEquals(
            expectedData,
            scanData
        )
    }
}
