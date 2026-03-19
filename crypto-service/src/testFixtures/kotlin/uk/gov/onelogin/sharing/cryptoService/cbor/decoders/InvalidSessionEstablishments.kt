package uk.gov.onelogin.sharing.cryptoService.cbor.decoders

import com.google.testing.junit.testparameterinjector.TestParameters
import com.google.testing.junit.testparameterinjector.TestParametersValuesProvider
import uk.gov.onelogin.sharing.cryptoService.SessionEstablishmentStub.INVALID_CBOR_MISSING_DATA_PARAMETER
import uk.gov.onelogin.sharing.cryptoService.SessionEstablishmentStub.INVALID_CBOR_MISSING_E_READER

@OptIn(ExperimentalStdlibApi::class)
class InvalidSessionEstablishments : TestParametersValuesProvider() {
    override fun provideValues(context: Context?): List<TestParameters.TestParametersValues?>? =
        listOf(
            "Malformed session establishment" to INVALID_CBOR_MISSING_DATA_PARAMETER,
            "Missing eReader key" to INVALID_CBOR_MISSING_E_READER
        ).map { (testName, input) ->
            TestParameters.TestParametersValues.builder()
                .name(testName)
                .addParameter(
                    "sessionEstablishmentBytes",
                    input.hexToByteArray()
                ).build()
        }
}
