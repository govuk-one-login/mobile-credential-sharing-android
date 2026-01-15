package uk.gov.onelogin.sharing.verifier.connect.parameters

import com.google.testing.junit.testparameterinjector.TestParameters
import com.google.testing.junit.testparameterinjector.TestParametersValuesProvider
import uk.gov.onelogin.sharing.core.presentation.permissions.FakeMultiplePermissionsStateStubs

class PermissionsToLogMessages : TestParametersValuesProvider() {
    override fun provideValues(context: Context?): List<TestParameters.TestParametersValues> =
        listOf(
            Triple(
                "All permissions are granted",
                FakeMultiplePermissionsStateStubs.bluetoothPermissionsGranted,
                "All required Bluetooth permissions have been granted"
            ),
            Triple(
                "All permissions are denied",
                FakeMultiplePermissionsStateStubs.bluetoothPermissionsDenied,
                "Bluetooth permissions were permanently denied"
            ),
            Triple(
                "Permissions required a rationale",
                FakeMultiplePermissionsStateStubs.bluetoothPermissionsDeniedWithRationale,
                "Bluetooth permissions were denied"
            )
        ).map { (name, input, expectedMessage) ->
            TestParameters.TestParametersValues.builder()
                .name(name)
                .addParameter(
                    "input",
                    input
                )
                .addParameter("expectedMessage", expectedMessage)
                .build()
        }
}
