package uk.gov.onelogin.sharing.verifier.scan.callbacks

import com.google.testing.junit.testparameterinjector.TestParameters.TestParametersValues
import com.google.testing.junit.testparameterinjector.TestParametersValuesProvider
import uk.gov.android.ui.componentsv2.camera.analyzer.qr.BarcodeSourceStub.Companion.unknown
import uk.gov.android.ui.componentsv2.camera.analyzer.qr.BarcodeSourceStub.Companion.urlQrCode
import uk.gov.android.ui.componentsv2.camera.qr.BarcodeScanResult
import uk.gov.onelogin.sharing.security.DecoderStub
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResult
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResultStubs.invalidBarcodeDataResultOne
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResultStubs.validBarcodeDataResult

/**
 * [TestParametersValuesProvider] implementation that facilitates parameterized testing for
 * the [VerifierScannerBarcodeScanCallback] behaviour.
 *
 * Maps [BarcodeScanResult] inputs to [BarcodeDataResult] outputs.
 *
 * Expects two parameters for a test function:
 * - `result`: The [BarcodeScanResult] input.
 * - `expectedData`: The [BarcodeDataResult] output.
 */
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
            BarcodeScanResult.Single(urlQrCode(DecoderStub.VALID_MDOC_URI)),
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
