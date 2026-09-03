package uk.gov.onelogin.sharing.bluetooth.api.central.mdoc

import com.google.testing.junit.testparameterinjector.KotlinTestParameters.testValuesIn
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.bluetooth.api.central.GattClientError

@RunWith(TestParameterInjector::class)
class CentralBluetoothTransportErrorTest {

    @Test
    fun `Transforms client errors to Transport errors`(
        @TestParameter inputs: Pair<GattClientError, CentralBluetoothTransportError> =
            testValuesIn(
                errorMapping
            )
    ) {
        val (error, expected) = inputs
        assertThat(
            CentralBluetoothTransportError.fromClientError(error),
            equalTo(expected)
        )
    }

    companion object {
        private val errorMapping = listOf(
            GattClientError.BLUETOOTH_PERMISSION_MISSING to
                CentralBluetoothTransportError.BLUETOOTH_PERMISSION_MISSING,
            GattClientError.BLUETOOTH_GATT_NOT_AVAILABLE to
                CentralBluetoothTransportError.GATT_NOT_AVAILABLE,
            GattClientError.SERVICE_NOT_FOUND to CentralBluetoothTransportError.SERVICE_NOT_FOUND,
            GattClientError.INVALID_SERVICE to CentralBluetoothTransportError.INVALID_SERVICE,
            GattClientError.FAILED_TO_SUBSCRIBE to
                CentralBluetoothTransportError.FAILED_TO_SUBSCRIBE,
            GattClientError.FAILED_TO_START to CentralBluetoothTransportError.FAILED_TO_START,
            GattClientError.SERVICE_DISCOVERED_ERROR to
                CentralBluetoothTransportError.INVALID_SERVICE,
            GattClientError.INVALID_MESSAGE_PREFIX to
                CentralBluetoothTransportError.INVALID_MESSAGE_PREFIX
        )
    }
}
