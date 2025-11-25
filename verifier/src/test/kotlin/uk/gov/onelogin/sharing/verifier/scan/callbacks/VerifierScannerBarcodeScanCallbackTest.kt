package uk.gov.onelogin.sharing.verifier.scan.callbacks

import androidx.test.ext.junit.runners.AndroidJUnit4
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
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog
import uk.gov.android.ui.componentsv2.camera.analyzer.qr.BarcodeSourceStub.Companion.asUrlBarcodes
import uk.gov.android.ui.componentsv2.camera.analyzer.qr.BarcodeSourceStub.Companion.unknown
import uk.gov.android.ui.componentsv2.camera.analyzer.qr.BarcodeSourceStub.Companion.urlQrCode
import uk.gov.android.ui.componentsv2.camera.qr.BarcodeScanResult
import uk.gov.onelogin.sharing.core.data.UriTestData.exampleUriOne
import uk.gov.onelogin.sharing.security.engagement.EngagementGeneratorStub.validMdocUri
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResult
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResultStubs.invalidBarcodeDataResultOne
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResultStubs.invalidBarcodeDataResultTwo
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResultStubs.validBarcodeDataResult

@RunWith(AndroidJUnit4::class)
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
    fun emptyScans() = performLoggingFlow(
        result = BarcodeScanResult.EmptyScan,
        expectedMessage = "Barcode data not found",
        expectedData = BarcodeDataResult.NotFound
    )

    @Test
    fun httpsUrlsAreInvalid() = performLoggingFlow(
        result = BarcodeScanResult.Single(urlQrCode(invalidBarcodeDataResultOne.data)),
        expectedMessage = invalidBarcodeDataResultOne.data,
        expectedData = invalidBarcodeDataResultOne
    )

    @Test
    fun mdocUrlsAreValid() {
        performLoggingFlow(
            result = BarcodeScanResult.Single(urlQrCode(validMdocUri)),
            expectedMessage = validMdocUri,
            expectedData = validBarcodeDataResult
        )
    }

    @Test
    fun singleUnknownScans() = performLoggingFlow(
        result = BarcodeScanResult.Single(unknown()),
        expectedMessage = "No URL found from single result!",
        expectedData = BarcodeDataResult.NotFound
    )

    @Test
    fun exceptions() = "This is a unit test!".run {
        performLoggingFlow(
            result = BarcodeScanResult.Failure(Exception(this)),
            expectedMessage = this,
            expectedData = BarcodeDataResult.NotFound
        )
    }

    @Test
    fun onlyChecksTheFirstBarcode() = performLoggingFlow(
        result =
        BarcodeScanResult.Success(
            listOf(
                invalidBarcodeDataResultOne.data,
                invalidBarcodeDataResultTwo.data
            ).asUrlBarcodes()
        ),
        expectedMessage = exampleUriOne,
        expectedData = invalidBarcodeDataResultOne
    ).also {
        assert(
            loggingFile.readLines().none {
                it.contains(invalidBarcodeDataResultTwo.data)
            }
        )
    }

    @Test
    fun scansWithoutUrlsCannotBeFound() = performLoggingFlow(
        result =
        BarcodeScanResult.Success(
            listOf(
                unknown(),
                unknown()
            )
        ),
        expectedMessage = "No URL found!",
        expectedData = BarcodeDataResult.NotFound
    )

    private fun performLoggingFlow(
        result: BarcodeScanResult,
        expectedMessage: String,
        expectedData: BarcodeDataResult
    ) = runTest {
        callback.onResult(result) { hasToggledScanner = true }

        val loggingOutput = loggingFile.readLines()
        assert(
            loggingOutput.any {
                it.startsWith(
                    "I/VerifierScannerBarcodeScanCallback: Obtained BarcodeScanResult: ${result::class.java.simpleName}"
                )
            }
        ) {
            "Couldn't find the \"$expectedMessage\" in: $loggingOutput"
        }

        assertEquals(
            expectedData,
            scanData
        )
    }
}
