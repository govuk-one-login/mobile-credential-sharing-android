package uk.gov.onelogin.sharing.prerequisites

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.prerequisites.evaluator.PrerequisiteEvaluator
import uk.gov.onelogin.sharing.prerequisites.state.BluetoothState
import uk.gov.onelogin.sharing.prerequisites.state.CameraState
import uk.gov.onelogin.sharing.prerequisites.state.LocationState

@ContributesBinding(AppScope::class)
@Inject
class PrerequisiteGateImpl(
    private val bluetoothEvaluator: PrerequisiteEvaluator<BluetoothState>,
    private val cameraEvaluator: PrerequisiteEvaluator<CameraState>,
    private val locationEvaluator: PrerequisiteEvaluator<LocationState>,
    private val logger: Logger
) : PrerequisiteGate {

    override fun evaluatePrerequisites(
        prerequisites: Iterable<Prerequisite>
    ): List<MissingPrerequisite> = prerequisites.mapNotNull { prerequisite ->
        when (prerequisite) {
            Prerequisite.BLUETOOTH -> bluetoothEvaluator.evaluate()
                ?.let(MissingPrerequisites::Bluetooth)

            Prerequisite.CAMERA -> cameraEvaluator.evaluate()
                ?.let(MissingPrerequisites::Camera)

            Prerequisite.LOCATION -> locationEvaluator.evaluate()
                ?.let(MissingPrerequisites::Location)

            else -> null
        }.also {
            logger.debug(
                logTag,
                "Performed prerequisite checks for: $prerequisites"
            )
        }
    }
}
