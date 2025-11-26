package uk.gov.onelogin.sharing.verifier.scan.callbacks

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.File
import java.io.FileOutputStream
import java.io.PrintStream
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

    private var url: Uri? = null
    private var hasToggledScanner = false

    private val callback = VerifierScannerBarcodeScanCallback {
        url = it
    }

    @Before
    fun setUp() {
        loggingFile = folder.newFile("barcodeScanResultLoggingCallbackOutputs.txt")
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
        expectedMessage = "Barcode data not found"
    )

    @Test
    fun singleUrlScans() = "https://this.is.a.unit.test".run {
        performLoggingFlow(
            result = BarcodeScanResult.Single(urlQrCode(this)),
            expectedMessage = this
        )
    }

    @Test
    fun singleUnknownScans() = performLoggingFlow(
        result = BarcodeScanResult.Single(unknown()),
        expectedMessage = "No URL found from single result!"
    )

    @Test
    fun exceptions() = "This is a unit test!".run {
        performLoggingFlow(
            result = BarcodeScanResult.Failure(Exception(this)),
            expectedMessage = this
        )
    }

    @Test
    fun successScansPrintTheFirstUrl() = performLoggingFlow(
        result =
            BarcodeScanResult.Success(
                listOf(
                    "https://this.is.a.unit.test",
                    "https://this.is.another.test"
                ).asUrlBarcodes()
            ),
        expectedMessage = "https://this.is.a.unit.test"
    ).also {
        assert(
            loggingFile.readLines().none {
                it.contains("https://this.is.another.test")
            }
        )
    }

    @Test
    fun successfulScansWithoutUrls() = performLoggingFlow(
        result =
            BarcodeScanResult.Success(
                listOf(
                    unknown(),
                    unknown()
                )
            ),
        expectedMessage = "No URL found!"
    )

    private fun performLoggingFlow(result: BarcodeScanResult, expectedMessage: String) = runTest {
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
    }
}
