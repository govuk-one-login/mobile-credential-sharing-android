package uk.gov.onelogin.sharing.orchestration.prerequisites.capability

import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.camera.lifecycle.ProcessCameraProvider
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteResponse
import uk.gov.onelogin.sharing.orchestration.prerequisites.camera.ProcessCameraProviderFactory
import uk.gov.onelogin.sharing.orchestration.prerequisites.capability.IncapableReasonMatchers.cannotCheckCamera
import uk.gov.onelogin.sharing.orchestration.prerequisites.capability.IncapableReasonMatchers.isMissingHardware
import uk.gov.onelogin.sharing.orchestration.prerequisites.matchers.PrerequisiteResponseMatchers.hasIncapableReason

class CapabilityPrerequisiteLayerTest {
    private val processCameraProvider: ProcessCameraProvider = mockk()
    private val context: Context = mockk()
    private val bluetoothManager: BluetoothManager = mockk()
    private var factory = ProcessCameraProviderFactory {
        processCameraProvider
    }

    private val logger = SystemLogger()
    private val capability by lazy {
        CapabilityPrerequisiteLayer(
            context,
            factory = factory,
            logger
        )
    }

    @Before
    fun setUp() {
        every {
            context.getSystemService(Context.BLUETOOTH_SERVICE)
        }.returns(bluetoothManager)
    }

    private fun verifyLogs(prerequisite: Prerequisite) {
        assert(
            logger.any {
                it.message.startsWith("Performed $prerequisite capability check.")
            }
        )
    }

    @Test
    fun `Bluetooth is incapable when unable to obtain a manager from the context`() = runTest {
        every {
            bluetoothManager.adapter
        }.returns(null)

        performJourney(
            Prerequisite.BLUETOOTH,
            hasIncapableReason(isMissingHardware())
        )
    }

    @Test
    fun `Bluetooth is capable when able to obtain a manager from the context`() = runTest {
        every {
            bluetoothManager.adapter
        }.returns(mockk())

        performJourney(
            Prerequisite.BLUETOOTH,
            nullValue()
        )
    }

    @Test
    fun `Camera is incapable when device has no rear-facing camera`() = runTest {
        every {
            processCameraProvider.hasCamera(any())
        }.returns(false)

        performJourney(
            Prerequisite.CAMERA,
            hasIncapableReason(isMissingHardware())
        )
    }

    @Test
    fun `Camera is incapable when unable to obtain a ProcessCameraProvider instance`() = runTest {
        factory = ProcessCameraProviderFactory {
            throw IllegalStateException("This is a unit test")
        }

        performJourney(
            Prerequisite.CAMERA,
            hasIncapableReason(cannotCheckCamera())
        )
    }

    @Test
    fun `Camera is capable when device has a rear-facing camera`() = runTest {
        every {
            processCameraProvider.hasCamera(any())
        } returns true

        performJourney(
            Prerequisite.CAMERA,
            nullValue()
        )
    }

    @Test
    fun `Unknown prerequisites always pass prerequisite check`() = runTest {
        performJourney(
            Prerequisite.UNKNOWN,
            nullValue()
        )
    }

    private fun performJourney(
        prerequisite: Prerequisite,
        matcher: Matcher<in PrerequisiteResponse>
    ) {
        assertThat(
            capability.checkCapability(prerequisite),
            matcher
        )
        verifyLogs(prerequisite)
    }
}
