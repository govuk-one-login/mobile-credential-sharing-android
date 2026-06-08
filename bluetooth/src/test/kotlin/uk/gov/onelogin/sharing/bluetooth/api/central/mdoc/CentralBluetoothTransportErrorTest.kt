package uk.gov.onelogin.sharing.bluetooth.api.central.mdoc

import com.google.testing.junit.testparameterinjector.KotlinTestParameters.testValuesIn
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.bluetooth.api.gatt.central.ClientError

@RunWith(TestParameterInjector::class)
class CentralBluetoothTransportErrorTest {

    @Test
    fun `Transforms client errors to Transport errors`(
        @TestParameter inputs: Pair<ClientError, CentralBluetoothTransportError> = testValuesIn(
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
            ClientError.BLUETOOTH_PERMISSION_MISSING to
                CentralBluetoothTransportError.BLUETOOTH_PERMISSION_MISSING,
            ClientError.BLUETOOTH_GATT_NOT_AVAILABLE to
                CentralBluetoothTransportError.GATT_NOT_AVAILABLE,
            ClientError.SERVICE_NOT_FOUND to CentralBluetoothTransportError.SERVICE_NOT_FOUND,
            ClientError.INVALID_SERVICE to CentralBluetoothTransportError.INVALID_SERVICE,
            ClientError.FAILED_TO_SUBSCRIBE to CentralBluetoothTransportError.FAILED_TO_SUBSCRIBE,
            ClientError.FAILED_TO_START to CentralBluetoothTransportError.FAILED_TO_START,
            ClientError.SERVICE_DISCOVERED_ERROR to CentralBluetoothTransportError.INVALID_SERVICE,
            ClientError.INVALID_MESSAGE_PREFIX to
                CentralBluetoothTransportError.INVALID_MESSAGE_PREFIX
        )
    }
}
