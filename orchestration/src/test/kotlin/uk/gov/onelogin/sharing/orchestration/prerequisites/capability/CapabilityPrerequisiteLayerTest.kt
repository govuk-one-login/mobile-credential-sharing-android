package uk.gov.onelogin.sharing.orchestration.prerequisites.capability

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import androidx.test.core.app.ApplicationProvider
import io.mockk.mockk
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.ble.BluetoothContext
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.capability.IncapableReasonMatchers.isMissingHardware
import uk.gov.onelogin.sharing.orchestration.prerequisites.matchers.PrerequisiteResponseMatchers.hasIncapableReason

@RunWith(RobolectricTestRunner::class)
class CapabilityPrerequisiteLayerTest {
    private val app = ApplicationProvider.getApplicationContext<Application>()
    private val mockAdapter: BluetoothAdapter = mockk()
    private var mockManager: BluetoothManager? = mockk()
    private val context by lazy {
        BluetoothContext(app, mockManager)
    }

    private val logger = SystemLogger()
    private val request = Prerequisite.BLUETOOTH
    private val capability by lazy {
        CapabilityPrerequisiteLayer(
            context,
            logger
        )
    }

    private fun verifyLogs(
        prerequisite: Prerequisite
    ) {
        assert(
            logger.any {
                it.message.startsWith("Performed $prerequisite capability check.")
            }
        )
    }

    @Test
    fun `Bluetooth is incapable when unable to obtain a manager from the context`() = runTest {
        mockManager = null

        assertThat(
            capability.checkCapability(Prerequisite.BLUETOOTH),
            hasIncapableReason(isMissingHardware())
        )
        verifyLogs(Prerequisite.BLUETOOTH)
    }

    @Test
    fun `Bluetooth is capable when able to obtain a manager from the context`() = runTest {
        assertThat(
            capability.checkCapability(Prerequisite.BLUETOOTH),
            nullValue(),
        )
    }
}
