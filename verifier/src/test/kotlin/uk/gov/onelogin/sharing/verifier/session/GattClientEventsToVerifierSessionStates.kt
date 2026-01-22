package uk.gov.onelogin.sharing.verifier.session

import com.google.testing.junit.testparameterinjector.TestParameters
import com.google.testing.junit.testparameterinjector.TestParametersValuesProvider
import uk.gov.android.ui.componentsv2.camera.qr.BarcodeScanResult
import uk.gov.onelogin.sharing.bluetooth.api.gatt.central.ClientError
import uk.gov.onelogin.sharing.bluetooth.api.gatt.central.GattClientEvent
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResult

class GattClientEventsToVerifierSessionStates : TestParametersValuesProvider() {
    override fun provideValues(context: Context?): List<TestParameters.TestParametersValues> =
        listOf(
            Triple(
                "GATT Services are connecting",
                GattClientEvent.Connecting,
                VerifierSessionState.Connecting
            ),
            Triple(
                "GATT Services connect to Bluetooth device address Strings",
                GattClientEvent.Connected("unit test address"),
                VerifierSessionState.Connected("unit test address")
            ),
            Triple(
                "GATT Services disonnect to Bluetooth device address Strings",
                GattClientEvent.Disconnected("unit test address"),
                VerifierSessionState.Disconnected("unit test address")
            ),
            Triple(
                "GATT Service errors pass client errors as Strings",
                GattClientEvent.Error(ClientError.BLUETOOTH_PERMISSION_MISSING),
                VerifierSessionState.Error(
                    ClientError.BLUETOOTH_PERMISSION_MISSING.toString()
                )
            ),
            Triple(
                "Invalid GATT Service configuration is a separate session state",
                GattClientEvent.Error(ClientError.INVALID_SERVICE),
                VerifierSessionState.Invalid
            ),
            Triple(
                "GATT service not found",
                GattClientEvent.Error(ClientError.SERVICE_NOT_FOUND),
                VerifierSessionState.ServiceNotFound
            )
        ).map { (name, input, expectedState) ->
            TestParameters.TestParametersValues.builder()
                .name(name)
                .addParameter(
                    "input",
                    input
                )
                .addParameter("expectedState", expectedState)
                .build()
        }
}
