package uk.gov.onelogin.sharing.cryptoService.scanner

import com.google.testing.junit.testparameterinjector.TestParameters
import com.google.testing.junit.testparameterinjector.TestParametersValuesProvider
import uk.gov.onelogin.sharing.cryptoService.DecoderStub.INVALID_VALID_MDOC_URI
import uk.gov.onelogin.sharing.cryptoService.DecoderStub.VALID_ENCODED_DEVICE_ENGAGEMENT
import uk.gov.onelogin.sharing.cryptoService.DecoderStub.VALID_MDOC_URI

class QrParserProvider : TestParametersValuesProvider() {
    override fun provideValues(context: Context?) = listOf(
        Triple("null", null, QrScanResult.NotFound),
        Triple("blank", "   ", QrScanResult.NotFound),
        Triple(
            "Parses a valid MDOC url",
            VALID_MDOC_URI,
            QrScanResult.Success(
                VALID_ENCODED_DEVICE_ENGAGEMENT
            )
        ),
        Triple(
            "Returns invalid for invalid URL",
            INVALID_VALID_MDOC_URI,
            QrScanResult.Invalid(INVALID_VALID_MDOC_URI)
        )
    ).map { (name, input, expected) ->
        TestParameters.TestParametersValues.builder()
            .name(name)
            .addParameter("input", input)
            .addParameter("expected", expected)
            .build()
    }
}
