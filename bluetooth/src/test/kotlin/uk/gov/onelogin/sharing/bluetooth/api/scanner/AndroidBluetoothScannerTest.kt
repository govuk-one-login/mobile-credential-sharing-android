package uk.gov.onelogin.sharing.bluetooth.api.scanner

import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowBluetoothAdapter
import org.robolectric.shadows.ShadowBluetoothLeScanner
import org.robolectric.shadows.ShadowBluetoothManager
import org.robolectric.shadows.ShadowLog
import uk.gov.onelogin.sharing.core.UUIDExtensions.toBytes
import uk.gov.onelogin.sharing.core.rules.ShadowLogFile

@RunWith(AndroidJUnit4::class)
@Config(
    shadows = [
        ShadowBluetoothLeScanner::class,
        ShadowBluetoothAdapter::class,
        ShadowBluetoothManager::class,
        ShadowLog::class
    ]
)
class AndroidBluetoothScannerTest {

    @get:Rule
    val logFile = ShadowLogFile(fileName = this::class.java.simpleName)

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant()

    lateinit var leScanner: BluetoothLeScanner

    @Before
    fun setUp() {
        leScanner =
            (
                ApplicationProvider
                    .getApplicationContext<Context>()
                    .getSystemService(Context.BLUETOOTH_SERVICE)
                    as BluetoothManager
                ).adapter.bluetoothLeScanner
    }

    @Test
    fun timeoutsAreLoggedAsWarnings() = runTest {
        val scanner = AndroidBluetoothScanner(
            getBluetoothScanner = { leScanner }
        )
        scanner.scan(
            scanningPeriodMilliseconds = -1L,
            callback = ScannerCallback.of(),
            peripheralServerModeUuids = listOf()
        )

        testScheduler.advanceUntilIdle()

        assertTrue(
            "Cannot find entry in log files: ${logFile.readLines()}",
            "W/${AndroidBluetoothScanner::class.java.simpleName}: " +
                "Timeout occurred when scanning for UUIDs."
                in logFile
        )
    }

    @Test
    fun requestedUuidsAreLogged() = runTest {
        val scanner = AndroidBluetoothScanner(
            getBluetoothScanner = { leScanner }
        )
        val uuid = UUID.randomUUID()
        val byteArray = uuid.toBytes()
        val byteArrays = listOf(byteArray)
        val uuidArrays = listOf(uuid)

        scanner.scan(
            scanningPeriodMilliseconds = 10000L,
            callback = ScannerCallback.of(),
            peripheralServerModeUuids = byteArrays
        )

        testScheduler.advanceUntilIdle()

        assertTrue(
            "Cannot find entry in log files: ${logFile.readLines()}",
            "D/${AndroidBluetoothScanner::class.java.simpleName}: " +
                "Scanning for UUIDs: $uuidArrays"
                in logFile
        )
    }
}
