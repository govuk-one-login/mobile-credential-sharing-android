package uk.gov.onelogin.sharing.orchestration.prerequisites

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import uk.gov.onelogin.sharing.orchestration.prerequisites.evaluator.PrerequisiteEvaluator
import uk.gov.onelogin.sharing.orchestration.prerequisites.state.BluetoothState
import uk.gov.onelogin.sharing.orchestration.prerequisites.state.CameraState
import uk.gov.onelogin.sharing.orchestration.prerequisites.state.LocationState

@ContributesBinding(AppScope::class)
@Inject
class PrerequisiteGateV2(
    private val bluetoothEvaluator: PrerequisiteEvaluator<BluetoothState>,
    private val cameraEvaluator: PrerequisiteEvaluator<CameraState>,
    private val locationEvaluator: PrerequisiteEvaluator<LocationState>
) : PrerequisiteGate.V2 {

    override fun evaluatePrerequisites(
        prerequisites: Iterable<Prerequisite>
    ): List<MissingPrerequisiteV2> = prerequisites.mapNotNull { prerequisite ->
        when (prerequisite) {
            Prerequisite.BLUETOOTH -> bluetoothEvaluator.evaluate()
                ?.let(MissingPrerequisiteV2::Bluetooth)

            Prerequisite.CAMERA -> cameraEvaluator.evaluate()
                ?.let(MissingPrerequisiteV2::Camera)

            Prerequisite.LOCATION -> locationEvaluator.evaluate()
                ?.let(MissingPrerequisiteV2::Location)

            else -> null
        }
    }
}
