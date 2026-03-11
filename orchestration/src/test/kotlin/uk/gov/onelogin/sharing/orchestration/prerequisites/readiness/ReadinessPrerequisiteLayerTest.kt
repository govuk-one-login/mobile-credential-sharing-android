package uk.gov.onelogin.sharing.orchestration.prerequisites.readiness

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.runner.RunWith
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteResponse
import uk.gov.onelogin.sharing.orchestration.prerequisites.camera.ProcessCameraProviderFactory
import uk.gov.onelogin.sharing.orchestration.prerequisites.matchers.PrerequisiteResponseMatchers.hasNotReadyReason
import uk.gov.onelogin.sharing.orchestration.prerequisites.readiness.NotReadyReasonMatchers.cameraAlreadyInUse
import uk.gov.onelogin.sharing.orchestration.prerequisites.readiness.NotReadyReasonMatchers.cannotCheckCamera
import uk.gov.onelogin.sharing.orchestration.prerequisites.readiness.NotReadyReasonMatchers.hasBluetoothTurnedOff

@RunWith(TestParameterInjector::class)
class ReadinessPrerequisiteLayerTest {
    private val context: Context = mockk()
    private val logger = SystemLogger()
    private val processCameraProvider: ProcessCameraProvider = mockk()
    private val bluetoothManager: BluetoothManager = mockk()
    private val bluetoothAdapter: BluetoothAdapter = mockk()
    private val cameraInfo: CameraInfo = mockk()
    private val cameraState: CameraState = mockk()
    private val cameraStateData: LiveData<CameraState> = MutableLiveData(cameraState)

    private var factory = ProcessCameraProviderFactory {
        processCameraProvider
    }
    private val readiness by lazy {
        ReadinessPrerequisiteLayer(
            context,
            factory,
            logger
        )
    }

    private val cameraAssertions: Map<CameraState.Type, Matcher<in PrerequisiteResponse>> =
        CameraState.Type.entries.associateWith {
            hasNotReadyReason(cameraAlreadyInUse())
        }.toMutableMap().also {
            it[CameraState.Type.CLOSED] = nullValue(PrerequisiteResponse::class.java)
        }

    @Before
    fun setUp() {
        every {
            context.getSystemService(Context.BLUETOOTH_SERVICE)
        } returns bluetoothManager
        every {
            bluetoothManager.adapter
        } returns bluetoothAdapter
        every {
            processCameraProvider.getCameraInfo(any())
        } returns cameraInfo
    }

    @Test
    fun `Bluetooth is ready whilst bluetooth is enabled`() = runTest {
        every {
            bluetoothAdapter.isEnabled
        } returns true

        performJourney(
            Prerequisite.BLUETOOTH,
            nullValue()
        )
    }

    @Test
    fun `Bluetooth isn't ready whilst bluetooth is disabled`() = runTest {
        every {
            bluetoothAdapter.isEnabled
        } returns false

        performJourney(
            Prerequisite.BLUETOOTH,
            hasNotReadyReason(hasBluetoothTurnedOff())
        )
    }

    @Test
    fun `Having no camera state due to not initialising then passes the check`() = runTest {
        every {
            cameraInfo.cameraState
        } returns MutableLiveData(null)

        performJourney(
            Prerequisite.CAMERA,
            nullValue()
        )
    }

    @Test
    fun `Camera readiness is based on the CameraState's type`(
        @TestParameter type: CameraState.Type
    ) = runTest {
        every {
            cameraInfo.cameraState
        } returns cameraStateData
        every {
            cameraState.type
        } returns type

        performJourney(
            Prerequisite.CAMERA,
            cameraAssertions[type]!!
        )
    }

    @Test
    fun `Camera isn't ready when unable to obtain a ProcessCameraProvider instance`() = runTest {
        factory = ProcessCameraProviderFactory {
            throw IllegalStateException("This is a unit test")
        }

        performJourney(
            Prerequisite.CAMERA,
            hasNotReadyReason(cannotCheckCamera())
        )
    }

    @Test
    fun `Unknown prerequisites always pass the readiness check`() = runTest {
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
            readiness.checkReadiness(prerequisite),
            matcher
        )
        assertTrue {
            logger.any {
                it.message.startsWith("Performed $prerequisite readiness check.")
            }
        }
    }
}
