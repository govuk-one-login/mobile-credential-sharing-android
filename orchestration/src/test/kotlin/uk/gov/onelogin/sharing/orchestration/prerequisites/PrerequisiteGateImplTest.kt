package uk.gov.onelogin.sharing.orchestration.prerequisites

import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.hasSize
import org.junit.runner.RunWith
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.orchestration.prerequisites.evaluator.PrerequisiteEvaluator
import uk.gov.onelogin.sharing.orchestration.prerequisites.matchers.MissingPrerequisiteMatchers.hasBluetoothState
import uk.gov.onelogin.sharing.orchestration.prerequisites.matchers.MissingPrerequisiteMatchers.hasCameraState
import uk.gov.onelogin.sharing.orchestration.prerequisites.matchers.MissingPrerequisiteMatchers.hasLocationState
import uk.gov.onelogin.sharing.orchestration.prerequisites.state.BluetoothState
import uk.gov.onelogin.sharing.orchestration.prerequisites.state.CameraState
import uk.gov.onelogin.sharing.orchestration.prerequisites.state.LocationState

@RunWith(TestParameterInjector::class)
class PrerequisiteGateImplTest {

    private var bluetoothResponse: BluetoothState? = null
    private var cameraResponse: CameraState? = null
    private var locationResponse: LocationState? = null

    private val bluetoothEvaluator = PrerequisiteEvaluator {
        bluetoothResponse
    }
    private val cameraEvaluator = PrerequisiteEvaluator {
        cameraResponse
    }
    private val locationEvaluator = PrerequisiteEvaluator {
        locationResponse
    }

    private val gate by lazy {
        PrerequisiteGateImpl(
            bluetoothEvaluator = bluetoothEvaluator,
            cameraEvaluator = cameraEvaluator,
            locationEvaluator = locationEvaluator,
            logger = SystemLogger()
        )
    }

    @Test
    fun `Passing prerequisite checks provide an empty list`(
        @TestParameter prerequisite: Prerequisite
    ) = runTest {
        val result = gate.evaluatePrerequisites(prerequisite)
        assertThat(
            result,
            hasSize(0)
        )
    }

    @Test
    fun `Wraps found bluetooth issues as a missing prerequisite`(
        @TestParameter state: BluetoothState
    ) = runTest {
        bluetoothResponse = state
        val result = gate.evaluatePrerequisites(Prerequisite.BLUETOOTH)
        assertThat(
            result,
            contains(
                hasBluetoothState(state)
            )
        )
    }

    @Test
    fun `Wraps found camera issues as a missing prerequisite`(@TestParameter state: CameraState) =
        runTest {
            cameraResponse = state
            val result = gate.evaluatePrerequisites(Prerequisite.CAMERA)
            assertThat(
                result,
                contains(
                    hasCameraState(state)
                )
            )
        }

    @Test
    fun `Wraps found location issues as a missing prerequisite`(
        @TestParameter state: LocationState
    ) = runTest {
        locationResponse = state
        val result = gate.evaluatePrerequisites(Prerequisite.LOCATION)
        assertThat(
            result,
            contains(
                hasLocationState(state)
            )
        )
    }

    @Test
    fun `Entries within the list map to failed prerequisites`() = runTest {
        bluetoothResponse = BluetoothState.PermissionNotGranted
        cameraResponse = CameraState.PermissionNotGranted
        locationResponse = LocationState.PermissionNotGranted

        val result = gate.evaluatePrerequisites(Prerequisite.entries)

        assertThat(
            result,
            allOf(
                hasSize(3),
                contains(
                    hasBluetoothState(bluetoothResponse!!),
                    hasCameraState(cameraResponse!!),
                    hasLocationState(locationResponse!!)
                )
            )
        )
    }
}
