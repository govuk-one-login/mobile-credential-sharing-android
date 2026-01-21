package uk.gov.onelogin.sharing.security.cbor.decoders

import com.google.testing.junit.testparameterinjector.TestParameters
import com.google.testing.junit.testparameterinjector.TestParametersValuesProvider
import uk.gov.onelogin.sharing.security.SessionEstablishmentStub.invalidCborMissingDataParameter
import uk.gov.onelogin.sharing.security.SessionEstablishmentStub.invalidCborMissingEReader

class InvalidSessionEstablishments : TestParametersValuesProvider() {
    override fun provideValues(context: Context?): List<TestParameters.TestParametersValues?>? =
        listOf(
            "Malformed session establishment" to invalidCborMissingDataParameter,
            "Missing eReader key" to invalidCborMissingEReader
        ).map { (testName, input) ->
            TestParameters.TestParametersValues.builder()
                .name(testName)
                .addParameter(
                    "sessionEstablishmentBytes",
                    input.hexToByteArray()
                ).build()
        }
}
