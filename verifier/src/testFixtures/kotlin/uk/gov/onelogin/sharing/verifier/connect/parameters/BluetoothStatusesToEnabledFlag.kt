package uk.gov.onelogin.sharing.verifier.connect.parameters

import com.google.testing.junit.testparameterinjector.TestParameters
import com.google.testing.junit.testparameterinjector.TestParametersValuesProvider
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStatus
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceStateMatchers

class BluetoothStatusesToEnabledFlag : TestParametersValuesProvider() {
    override fun provideValues(context: Context?): List<TestParameters.TestParametersValues> =
        listOf(
            Triple(
                "OFF status is considered to be disabled",
                BluetoothStatus.OFF,
                ConnectWithHolderDeviceStateMatchers.hasBluetoothDisabled()
            ),
            Triple(
                "ON status is considered to be enabled",
                BluetoothStatus.ON,
                ConnectWithHolderDeviceStateMatchers.hasBluetoothEnabled()
            )
        ).map { (name, status, assertion) ->
            TestParameters.TestParametersValues.builder()
                .name(name)
                .addParameter(
                    "status",
                    status
                )
                .addParameter("assertion", assertion)
                .build()
        }
}
