package uk.gov.onelogin.sharing.models.mdoc.sessionData

import com.google.testing.junit.testparameterinjector.TestParameters
import com.google.testing.junit.testparameterinjector.TestParametersValuesProvider
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMapper

class ExampleSessionDataDtoInputs : TestParametersValuesProvider() {
    private val statusOnlyDtoList = SessionDataStatus.entries.map {
        "Status only: ${it.name}" to SessionDataDto(status = it.code)
    }

    private val inputs = listOf(
        "Empty SessionDataDto" to SessionDataDtoStubs.emptySessionDataDto,
        "ISO spec example" to CborMapper.default.readValue(
            SessionDataDtoStubs.validSessionDataDtoBytes,
            SessionDataDto::class.java
        )
    ) + statusOnlyDtoList

    override fun provideValues(context: Context?): List<TestParameters.TestParametersValues?> =
        inputs.map { (name, dto) ->
            TestParameters.TestParametersValues.builder()
                .name(name)
                .addParameter("dto", dto)
                .build()
        }
}
