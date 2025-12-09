package uk.gov.onelogin.sharing.bluetooth.api.scanner

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.le.ScanResult
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowBluetoothAdapter
import org.robolectric.shadows.ShadowLog
import uk.gov.onelogin.sharing.core.rules.ShadowLogFile

/**
 * Suppresses `DEPRECATION` due to the use of [ShadowBluetoothAdapter]'s method of obtaining a
 * [BluetoothAdapter].
 */
@RunWith(AndroidJUnit4::class)
@Config(shadows = [ShadowLog::class])
@Suppress("DEPRECATION")
class ConnectDevicesToGattServerTest {
    @get:Rule
    val logFile = ShadowLogFile(fileName = this::class.java.simpleName)

    private val gattDevices: MutableList<BluetoothGatt> = mutableListOf()

    private val macAddress = "00:11:22:33:AA:BB"
    private val device: BluetoothDevice = mockk()
    private val bluetoothGatt: BluetoothGatt = mockk()
    private val scanResult: ScanResult = mockk()
    private val bluetoothCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            Log.d(
                ConnectDevicesToGattServerTest::class.java.simpleName,
                "${gatt?.device?.address}: Connection state changed: $newState"
            )
        }
    }

    private val callback: ConnectDevicesToGattServer by lazy {
        ConnectDevicesToGattServer(
            context = ApplicationProvider.getApplicationContext(),
            callback = bluetoothCallback,
            onConnectToGattServer = {
                gattDevices.add(it)
            }
        )
    }

    @Before
    fun setUp() {
        every { scanResult.device }.returns(device)
        every { bluetoothGatt.device }.returns(device)
        every { device.address }.returns(macAddress)
    }

    @Test
    fun gattCallbackObtainsProvidedGattDevice() = runTest {
        every {
            device.connectGatt(any(), any(), eq(bluetoothCallback))
        }.answers {
            bluetoothCallback.onConnectionStateChange(
                bluetoothGatt,
                status = BluetoothGatt.STATE_CONNECTING,
                newState = BluetoothGatt.STATE_CONNECTED
            )
            bluetoothGatt
        }

        callback.onResult(-1, scanResult)

        assertTrue(
            "Cannot find expected entry in android logs: ${logFile.readLines()}"
        ) {
            "D/${ConnectDevicesToGattServerTest::class.java.simpleName}: " +
                "$macAddress: " +
                "Connection state changed: " +
                BluetoothGatt.STATE_CONNECTED in logFile
        }

        assertTrue(
            "Bluetooth GATT device wasn't added via callback."
        ) {
            bluetoothGatt in gattDevices
        }
    }

    @Test
    fun connectionErrorsAreOnlyLogged() = runTest {
        val exception = IllegalArgumentException("This is a unit test!")
        every {
            device.connectGatt(any(), any(), any())
        }.throws(exception)

        callback.onResult(-1, scanResult)

        assertTrue(
            "Cannot find expected entry in android logs: ${logFile.readLines()}"
        ) {
            logFile.any {
                it.startsWith(
                    "E/${ConnectDevicesToGattServer::class.java.simpleName}: " +
                        "Couldn't connect to GATT server: $macAddress"
                )
            }
        }
    }

    @Test
    fun callbackFailuresAreOnlyLogged() = runTest {
        callback.onFailure(ScannerFailure.INTERNAL_ERROR)

        assertTrue(
            "Cannot find expected entry in android logs: ${logFile.readLines()}"
        ) {
            "E/${ConnectDevicesToGattServer::class.java.simpleName}: " +
                "Bluetooth scanning failed: ${ScannerFailure.INTERNAL_ERROR}" in logFile
        }
    }
}
