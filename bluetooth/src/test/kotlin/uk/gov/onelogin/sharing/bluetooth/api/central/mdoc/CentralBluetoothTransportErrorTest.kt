package uk.gov.onelogin.sharing.bluetooth.api.central.mdoc

import com.google.testing.junit.testparameterinjector.KotlinTestParameters.testValuesIn
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.bluetooth.api.central.ClientClientError

@RunWith(TestParameterInjector::class)
class CentralBluetoothTransportErrorTest {

    @Test
    fun `Transforms client errors to Transport errors`(
        @TestParameter inputs: Pair<ClientClientError, CentralBluetoothTransportError> =
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
            ClientClientError.BLUETOOTH_PERMISSION_MISSING to
                CentralBluetoothTransportError.BLUETOOTH_PERMISSION_MISSING,
            ClientClientError.BLUETOOTH_GATT_NOT_AVAILABLE to
                CentralBluetoothTransportError.GATT_NOT_AVAILABLE,
            ClientClientError.SERVICE_NOT_FOUND to CentralBluetoothTransportError.SERVICE_NOT_FOUND,
            ClientClientError.INVALID_SERVICE to CentralBluetoothTransportError.INVALID_SERVICE,
            ClientClientError.FAILED_TO_SUBSCRIBE to
                CentralBluetoothTransportError.FAILED_TO_SUBSCRIBE,
            ClientClientError.FAILED_TO_START to CentralBluetoothTransportError.FAILED_TO_START,
            ClientClientError.SERVICE_DISCOVERED_ERROR to
                CentralBluetoothTransportError.INVALID_SERVICE,
            ClientClientError.INVALID_MESSAGE_PREFIX to
                CentralBluetoothTransportError.INVALID_MESSAGE_PREFIX
        )
    }
}
