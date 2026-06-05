package uk.gov.onelogin.sharing.bluetooth.internal.core

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.namedTestValuesIn
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.collection.IsCollectionWithSize.hasSize
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStatus

@RunWith(TestParameterInjector::class)
class BluetoothStateBroadcastReceiverTest {
    private val received = mutableListOf<BluetoothStatus>()

    private val receiver = BluetoothStateBroadcastReceiver {
        received.add(it)
    }

    private val context: Context = mockk(relaxed = true)
    private val intent: Intent = mockk(relaxed = true)

    @Test
    fun `Receiving unrelated actions does nothing`() {
        every {
            intent.action
        } returns BluetoothAdapter.ACTION_REQUEST_ENABLE

        receiver.onReceive(context, intent)

        assertThat(
            received,
            hasSize(0)
        )
    }

    @Test
    fun `Transforms android bluetooth state to a status`(
        @TestParameter inputs: Pair<Int, BluetoothStatus> = namedTestValuesIn(statesToStatuses),
    ) {
        val (adapterState, expected) = inputs

        every {
            intent.action
        } returns BluetoothAdapter.ACTION_STATE_CHANGED
        every {
            intent.getIntExtra(
                BluetoothAdapter.EXTRA_STATE,
                any()
            )
        } returns adapterState

        receiver.onReceive(context, intent)

        assertThat(
            received.first(),
            equalTo(expected)
        )
    }

    companion object {
        private val statesToStatuses = mapOf(
            "ON" to (BluetoothAdapter.STATE_ON to BluetoothStatus.ON),
            "OFF" to (BluetoothAdapter.STATE_OFF to BluetoothStatus.OFF),
            "TURNING_ON" to (BluetoothAdapter.STATE_TURNING_ON to BluetoothStatus.TURNING_ON),
            "TURNING_OFF" to (BluetoothAdapter.STATE_TURNING_OFF to BluetoothStatus.TURNING_OFF),
            "ERROR -> UNKNOWN" to (BluetoothAdapter.ERROR to BluetoothStatus.UNKNOWN),
        )
    }
}
