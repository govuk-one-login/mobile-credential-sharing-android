package uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc

import com.google.testing.junit.testparameterinjector.KotlinTestParameters.testValuesIn
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import io.mockk.mockk
import kotlin.test.Test
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.junit.runner.RunWith
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.api.gatt.peripheral.GattServerError
import uk.gov.onelogin.sharing.bluetooth.api.gatt.peripheral.GattServerEvent
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc.PeripheralBluetoothStateMatchers.hasAddress
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc.PeripheralBluetoothStateMatchers.hasMessage
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc.PeripheralBluetoothStateMatchers.hasSessionEnd
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc.PeripheralBluetoothStateMatchers.hasStatus
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc.PeripheralBluetoothStateMatchers.hasTransportError
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc.PeripheralBluetoothStateMatchers.isConnected
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc.PeripheralBluetoothStateMatchers.isDisconnected
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc.PeripheralBluetoothStateMatchers.isEnded
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc.PeripheralBluetoothStateMatchers.isError
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc.PeripheralBluetoothStateMatchers.isMessageReceived
import uk.gov.onelogin.sharing.bluetooth.internal.core.SessionEndStates

@RunWith(TestParameterInjector::class)
class GattServerEventToPeripheralBluetoothStateTest {
    private val logger = SystemLogger()
    private val transformer = GattServerEventToPeripheralBluetoothState(logger)

    @Test
    fun `Converts server events to peripheral bluetooth states`(
        @TestParameter inputs: Pair<GattServerEvent, Matcher<in PeripheralBluetoothState>> =
            testValuesIn(mapping)
    ) {
        val (event, assertion) = inputs
        assertThat(transformer.transform(event), assertion)
    }

    companion object {
        private const val DUMMY_STRING = "this is a unit test"
        private const val DUMMY_BOOLEAN = true
        private val dummyByteArray = byteArrayOf(0x01, 0x02)

        private val mapping: List<Pair<GattServerEvent, Matcher<in PeripheralBluetoothState>>> =
            listOf(
                GattServerEvent.Connected(DUMMY_STRING) to isConnected(
                    hasAddress(DUMMY_STRING)
                ),
                GattServerEvent.Disconnected(
                    address = DUMMY_STRING,
                    isSessionEnd = DUMMY_BOOLEAN
                ) to isDisconnected(
                    allOf(
                        hasAddress(DUMMY_STRING),
                        hasSessionEnd(DUMMY_BOOLEAN)
                    )
                ),
                GattServerEvent.Error(
                    GattServerError.GATT_NOT_AVAILABLE
                ) to isError(
                    hasTransportError(PeripheralBluetoothTransportError.GATT_NOT_AVAILABLE)
                ),
                GattServerEvent.ServiceAdded(
                    Int.MAX_VALUE,
                    mockk(relaxed = true)
                ) to nullValue(),
                GattServerEvent.ServiceStopped to nullValue(),
                GattServerEvent.UnsupportedEvent(
                    DUMMY_STRING,
                    Int.MAX_VALUE,
                    Int.MIN_VALUE
                ) to nullValue(),
                GattServerEvent.SessionStarted to nullValue(),
                GattServerEvent.SessionEnd(
                    SessionEndStates.SUCCESS
                ) to isEnded(hasStatus(SessionEndStates.SUCCESS)),
                GattServerEvent.MessageReceived(
                    dummyByteArray
                ) to isMessageReceived(hasMessage(dummyByteArray))
            )
    }
}