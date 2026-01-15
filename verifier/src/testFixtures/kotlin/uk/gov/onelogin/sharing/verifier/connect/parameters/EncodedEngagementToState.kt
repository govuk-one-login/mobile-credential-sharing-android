package uk.gov.onelogin.sharing.verifier.connect.parameters

import com.google.testing.junit.testparameterinjector.TestParameters
import com.google.testing.junit.testparameterinjector.TestParametersValuesProvider
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers
import uk.gov.onelogin.sharing.security.DecoderStub
import uk.gov.onelogin.sharing.security.cbor.dto.DeviceEngagementDto
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceStateMatchers
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResultStubs

class EncodedEngagementToState : TestParametersValuesProvider() {
    override fun provideValues(context: Context?): List<TestParameters.TestParametersValues> =
        listOf(
            Triple(
                "Valid data also updates Device Engagement DTO",
                BarcodeDataResultStubs.validBarcodeDataResult.data,
                Matchers.allOf(
                    ConnectWithHolderDeviceStateMatchers.hasBase64EncodedEngagement(
                        BarcodeDataResultStubs.validBarcodeDataResult.data
                    ),
                    ConnectWithHolderDeviceStateMatchers.hasDeviceEngagementDto(
                        DecoderStub.validDeviceEngagementDto
                    )
                )
            ),
            Triple(
                "Invalid data keeps Device Engagement DTO as null",
                BarcodeDataResultStubs.undecodeableBarcodeDataResult.data,
                CoreMatchers.allOf(
                    ConnectWithHolderDeviceStateMatchers.hasBase64EncodedEngagement(
                        BarcodeDataResultStubs.undecodeableBarcodeDataResult.data
                    ),
                    ConnectWithHolderDeviceStateMatchers.hasDeviceEngagementDto(
                        CoreMatchers.nullValue(
                            DeviceEngagementDto::class.java
                        )
                    )
                )
            )
        ).map { (name, input, assertion) ->
            TestParameters.TestParametersValues.builder()
                .name(name)
                .addParameter(
                    "input",
                    input
                )
                .addParameter("assertion", assertion)
                .build()
        }
}
