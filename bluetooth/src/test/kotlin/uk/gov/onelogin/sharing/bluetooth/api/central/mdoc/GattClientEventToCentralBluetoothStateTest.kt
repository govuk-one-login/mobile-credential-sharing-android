package uk.gov.onelogin.sharing.bluetooth.api.central.mdoc

import com.google.testing.junit.testparameterinjector.KotlinTestParameters.testValuesIn
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import java.util.UUID
import kotlin.test.Test
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.junit.runner.RunWith
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.api.central.GattClientError
import uk.gov.onelogin.sharing.bluetooth.api.central.GattClientEvent
import uk.gov.onelogin.sharing.bluetooth.api.central.mdoc.CentralBluetoothStateMatchers.hasAddress
import uk.gov.onelogin.sharing.bluetooth.api.central.mdoc.CentralBluetoothStateMatchers.hasSessionEnd
import uk.gov.onelogin.sharing.bluetooth.api.central.mdoc.CentralBluetoothStateMatchers.hasUuid
import uk.gov.onelogin.sharing.bluetooth.api.central.mdoc.CentralBluetoothStateMatchers.hasValue
import uk.gov.onelogin.sharing.bluetooth.api.central.mdoc.CentralBluetoothStateMatchers.isBluetoothEnded
import uk.gov.onelogin.sharing.bluetooth.api.central.mdoc.CentralBluetoothStateMatchers.isConnected
import uk.gov.onelogin.sharing.bluetooth.api.central.mdoc.CentralBluetoothStateMatchers.isDisconnected
import uk.gov.onelogin.sharing.bluetooth.api.central.mdoc.CentralBluetoothStateMatchers.isError
import uk.gov.onelogin.sharing.bluetooth.api.central.mdoc.CentralBluetoothStateMatchers.isMessage
import uk.gov.onelogin.sharing.bluetooth.internal.core.SessionEndStates

@RunWith(TestParameterInjector::class)
class GattClientEventToCentralBluetoothStateTest {

    private val logger = SystemLogger()
    private val transformer = GattClientEventToCentralBluetoothState(logger)

    @Test
    fun `Converts client events to central bluetooth states`(
        @TestParameter inputs: Pair<GattClientEvent, Matcher<in CentralBluetoothState>> =
            testValuesIn(mapping)
    ) {
        val (event, assertion) = inputs
        assertThat(transformer.transform(event), assertion)
    }

    companion object {
        private const val DUMMY_STRING = "This is a unit test"
        private const val DUMMY_BOOLEAN = false
        private val dummyUuid = UUID.randomUUID()
        private val dummyByteArray = byteArrayOf(0x01, 0x02)

        private val mapping: List<Pair<GattClientEvent, Matcher<in CentralBluetoothState>>> =
            listOf(
                GattClientEvent.Connecting to equalTo(CentralBluetoothState.Connecting),
                GattClientEvent.Connected(DUMMY_STRING) to isConnected(hasAddress(DUMMY_STRING)),
                GattClientEvent.Disconnected(DUMMY_STRING, DUMMY_BOOLEAN) to isDisconnected(
                    allOf(
                        hasAddress(DUMMY_STRING),
                        hasSessionEnd(DUMMY_BOOLEAN)
                    )
                ),

                GattClientEvent.ConnectionStateStarted to equalTo(
                    CentralBluetoothState.ConnectionStateStarted
                ),

                GattClientEvent.Error(GattClientError.BLUETOOTH_PERMISSION_MISSING) to isError(
                    equalTo(
                        CentralBluetoothTransportError.BLUETOOTH_PERMISSION_MISSING
                    )
                ),
                GattClientEvent.SessionEnd(SessionEndStates.SUCCESS) to isBluetoothEnded(
                    equalTo(SessionEndStates.SUCCESS)
                ),
                GattClientEvent.Message(
                    uuid = dummyUuid,
                    value = dummyByteArray
                ) to isMessage(
                    allOf(
                        hasUuid(dummyUuid),
                        hasValue(dummyByteArray)
                    )
                ),
                GattClientEvent.UnsupportedEvent(
                    DUMMY_STRING,
                    Int.MAX_VALUE,
                    Int.MAX_VALUE
                ) to nullValue()
            )
    }
}
