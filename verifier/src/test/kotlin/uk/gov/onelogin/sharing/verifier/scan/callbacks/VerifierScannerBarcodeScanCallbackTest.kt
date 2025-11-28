package uk.gov.onelogin.sharing.verifier.scan.callbacks

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterValue
import com.google.testing.junit.testparameterinjector.TestParameterValuesProvider
import com.google.testing.junit.testparameterinjector.TestParameters
import com.google.testing.junit.testparameterinjector.TestParameters.TestParametersValues
import com.google.testing.junit.testparameterinjector.TestParametersValuesProvider
import java.io.File
import java.io.FileOutputStream
import java.io.PrintStream
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog
import uk.gov.android.ui.componentsv2.camera.analyzer.qr.BarcodeSourceStub.Companion.asUrlBarcodes
import uk.gov.android.ui.componentsv2.camera.analyzer.qr.BarcodeSourceStub.Companion.unknown
import uk.gov.android.ui.componentsv2.camera.analyzer.qr.BarcodeSourceStub.Companion.urlQrCode
import uk.gov.android.ui.componentsv2.camera.qr.BarcodeScanResult
import uk.gov.onelogin.sharing.core.data.UriTestData.exampleUriOne
import uk.gov.onelogin.sharing.security.DecoderStub.VALID_MDOC_URI
import uk.gov.onelogin.sharing.security.engagement.EngagementGeneratorStub.encodedEngagement
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResult
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResultStubs.invalidBarcodeDataResultOne
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResultStubs.invalidBarcodeDataResultTwo
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResultStubs.validBarcodeDataResult

@RunWith(RobolectricTestParameterInjector::class)
@Config(
    shadows = [ShadowLog::class]
)
class VerifierScannerBarcodeScanCallbackTest {
    @get:Rule
    var folder: TemporaryFolder = TemporaryFolder()

    private lateinit var loggingFile: File
    private lateinit var fileOutputStream: FileOutputStream
    private lateinit var printStream: PrintStream

    private var scanData: BarcodeDataResult? = null
    private var hasToggledScanner = false

    private val callback = VerifierScannerBarcodeScanCallback {
        scanData = it
    }

    @Before
    fun setUp() {
        loggingFile = folder.newFile("VerifierScannerBarcodeScanCallbackTest.txt")
        fileOutputStream = FileOutputStream(loggingFile)
        printStream = PrintStream(fileOutputStream)

        ShadowLog.stream = printStream
    }

    @After
    fun tearDown() {
        fileOutputStream.close()
        printStream.close()
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
            loggingFile.readLines().none {
                it.contains(invalidBarcodeDataResultTwo.data)
            }
        )
    }

    @Test
    @TestParameters(valuesProvider = VerifierScannerBarcodeScanCallbackProvider::class)
    fun performLoggingFlow(result: BarcodeScanResult, expectedData: BarcodeDataResult) = runTest {
        callback.onResult(result) { hasToggledScanner = true }

        val loggingOutput = loggingFile.readLines()
        assert(
            loggingOutput.any {
                it.startsWith(
                    "I/VerifierScannerBarcodeScanCallback: Obtained BarcodeScanResult: ${result::class.java.simpleName}"
                )
            }
        ) {
            "Couldn't find a \"${result::class.java.simpleName}\" entry in: $loggingOutput"
        }

        assertEquals(
            expectedData,
            scanData
        )
    }

    class VerifierScannerBarcodeScanCallbackProvider : TestParametersValuesProvider() {
        override fun provideValues(context: Context?): List<TestParametersValues> = listOf(
            Triple(
                "Empty scan",
                BarcodeScanResult.EmptyScan,
                BarcodeDataResult.NotFound
            ),
            Triple(
                "HTTP URLs are invalid",
                BarcodeScanResult.Single(urlQrCode(invalidBarcodeDataResultOne.data)),
                invalidBarcodeDataResultOne
            ),
            Triple(
                "mdoc URLs are valid",
                BarcodeScanResult.Single(urlQrCode(VALID_MDOC_URI)),
                validBarcodeDataResult
            ),
            Triple(
                "An Unknown barcode is considered to be not found",
                BarcodeScanResult.Single(unknown()),
                BarcodeDataResult.NotFound
            ),
            Triple(
                "Barcode failures are considered to be not found",
                BarcodeScanResult.Failure(Exception("This is a unit test!")),
                BarcodeDataResult.NotFound
            )
        ).map { (name, result, expectedData) ->
            TestParametersValues.builder()
                .name(name)
                .addParameter(
                    "result",
                    result
                )
                .addParameter("expectedData", expectedData)
                .build()
        }
    }
}
